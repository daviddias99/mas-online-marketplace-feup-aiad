package agents;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import agents.strategies.counter_offer.*;
import behaviours.NegotiateBuyer;
import models.Product;
import utils.CoolFormatter;
import utils.ProductQuantity;
import utils.TerminationListener;

public class Buyer extends Agent {

    // The products map contains pairs where the values are true if the
    // buyer as acquired the key product.
    private Map<Product, List<ProductStatus>> products = new ConcurrentHashMap<>();
    private CounterOfferStrategy counterOfferStrategy;
    private float moneySpent;
    private int patience;
    private Set<AID> blackList;
    private transient Logger logger;
    private ParallelBehaviour negotiationsBehaviour;
    private TerminationListener terminationListener;

    public void setTerminationListener(TerminationListener listener) {
        this.terminationListener = listener;
    }

    public Map<Product, Integer> getProductsBought() {
        Map<Product, Integer> res = new HashMap<>();
        int amountBought;
        for (Map.Entry<Product, List<ProductStatus>> entry : this.products.entrySet()) {
            amountBought = 0;
            for(ProductStatus status : entry.getValue())
                if (status == ProductStatus.BOUGHT)
                    amountBought++;
            res.put(entry.getKey(), amountBought);
        }

        return res;
    }

    enum ProductStatus {
        TRYING, BOUGHT, NO_SELLER,
    }

    @JsonCreator
    public Buyer(@JsonProperty("products") ProductQuantity[] products,
            @JsonProperty("counterOfferStrategy") String counterOfferStrategy, @JsonProperty("patience") int patience) {
        if (patience > 100 || patience < 0)
            throw new IllegalArgumentException("Patience must be from 0 to 100 and was " + patience);
        for (int i = 0; i < products.length; i++){
            List<ProductStatus> statusList = new ArrayList<>();
            for(int j = 0; j < products[i].getQuantity(); j++)
                statusList.add(ProductStatus.TRYING);
            this.products.put(products[i].getProduct(), statusList);
        }

        this.terminationListener = null;
        this.moneySpent = 0;
        this.counterOfferStrategy = CounterOfferStrategyFactory.get(counterOfferStrategy);
        this.blackList = new HashSet<>();
        this.patience = patience;
    }

    public CounterOfferStrategy getCounterOfferStrategy() {
        return counterOfferStrategy;
    }

    public void addScammer(AID seller) {
        this.blackList.add(seller);
    }

    public boolean isScammer(AID seller) {
        return this.blackList.contains(seller);
    }

    public Logger logger() {
        return this.logger;
    }

    // The buyer currently
    @Override
    protected void setup() {
        this.setupLogger();
        this.logger.info("- START: " + this);

        // Buyers sleep to allow for seller setup
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            this.logger.warning(String.format("> %s was interrupted while sleeping, ending.%n", this.getLocalName()));
            Thread.currentThread().interrupt();
        }

        // Ask prices of each product to sellers. The ask price behaviour choses the
        // seller with which to negotiate
        // The ask price behaviour will start the negotiation with the chosen seller.
        negotiationsBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        for (Map.Entry<Product, List<ProductStatus>> p : this.products.entrySet())
            for(int i = 0; i < p.getValue().size(); i++)
                negotiationsBehaviour.addSubBehaviour(new NegotiateBuyer(p.getKey(), i, this, new ACLMessage(ACLMessage.CFP)));

        this.addBehaviour(negotiationsBehaviour);
    }

    //
    // Helpers
    //
    public int getPatience() {
        return patience;
    }

    private void setupLogger() {
        this.logger = Logger.getLogger(this.getLocalName());
        this.logger.setUseParentHandlers(false);
        File dir = new File("logs/");
        if (!dir.exists())
            dir.mkdir();

        try {
            FileHandler fh = new FileHandler("logs/" + this.getLocalName() + ".log");
            this.logger.addHandler(fh);
            fh.setFormatter(new CoolFormatter());
        } catch (SecurityException | IOException e) {
            System.out.printf("</!\\ %s experienced an error while creating a logger%n", this.getLocalName());
            e.printStackTrace();
        }
    }

    public Set<Product> getProducts() {
        return this.products.keySet();
    }

    public ParallelBehaviour getBehaviour() {
        return this.negotiationsBehaviour;
    }

    // Get products that have yet to be bough by the buyer
    // public Set<Product> getMissingProducts() {
    //     return (this.products.entrySet().stream().filter(map -> map.getValue() == ProductStatus.TRYING)
    //             .collect(Collectors.toMap(Entry::getKey, Entry::getValue))).keySet();
    // }

    @Override
    public String toString() {
        if (this.getLocalName() != null)
            return this.getLocalName() + "{" + "products=" + this.products + "}";
        return "Buyer{" + "products=" + this.products + "}";
    }

    public synchronized void changeMoneySpent(float variance){
        this.moneySpent += variance;
    }

    public float getMoneySpent() {
        return this.moneySpent;
    }

    public void receivedProduct(Product product, int index) {
        List<ProductStatus> status = this.products.get(product);
        status.set(index, ProductStatus.BOUGHT); 
        this.products.put(product, status);
    }

    public void noSellerForProduct(Product product, int index) {
        List<ProductStatus> status = this.products.get(product);
        status.set(index, ProductStatus.NO_SELLER); 
        this.products.put(product, status);
    }

    public boolean finished() {

        for (Map.Entry<Product, List<ProductStatus>> p : this.products.entrySet())
            for(ProductStatus status : p.getValue())
                if (status == ProductStatus.TRYING)
                    return false;

        return true;
    }
    
    @Override
    public void takeDown() {
        for (Handler h: this.logger.getHandlers())
            h.close();

        if (this.terminationListener != null) {
            terminationListener.terminated(this);
        }
    }

    public boolean isBuying(Product product, int index) {
        List<ProductStatus> status = this.products.get(product);
        return status.get(index) == ProductStatus.TRYING;
    }
}
