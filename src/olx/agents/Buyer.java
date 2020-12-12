package olx.agents;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jade.core.AID;
import sajas.core.behaviours.SequentialBehaviour;
import sajas.core.Agent;
import jade.lang.acl.ACLMessage;
import olx.Olx;
import olx.agents.strategies.counter_offer.*;
import olx.behaviours.NegotiateBuyer;
import olx.draw.Edge;
import olx.draw.NetworkAgent;
import olx.draw.OlxNetwork;
import olx.models.Product;
import uchicago.src.sim.network.DefaultDrawableNode;
import olx.utils.CoolFormatter;
import olx.utils.ProductQuantity;
import olx.utils.TerminationListener;

public class Buyer extends Agent implements NetworkAgent {

    // The products map contains pairs where the values are true if the
    // buyer as acquired the key product.
    private Map<Product, List<ProductStatus>> products = new ConcurrentHashMap<>();
    private CounterOfferStrategy counterOfferStrategy;
    private float moneySpent;
    private int patience;
    private Set<AID> blackList;
    private transient Logger logger;
    private SequentialBehaviour negotiationsBehaviour;
    private TerminationListener terminationListener;
    private DefaultDrawableNode node;
    private LinkedList<Edge> lastEdges = new LinkedList<>();

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

    public DefaultDrawableNode getNode() {
        return this.node;
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

    public Buyer(Buyer buyer){
        for (Map.Entry<Product, List<ProductStatus>> entry : buyer.products.entrySet()){
            List<ProductStatus> statusList = new ArrayList<>();
            for(int j = 0; j < entry.getValue().size(); j++)
                statusList.add(ProductStatus.TRYING);
            this.products.put(entry.getKey(), statusList);
        }

        this.terminationListener = buyer.terminationListener;
        this.moneySpent = 0;
        this.counterOfferStrategy = buyer.getCounterOfferStrategy();
        this.blackList = new HashSet<>();
        this.patience = buyer.patience;
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
        if(!Olx.logging) this.logger.setLevel(Level.OFF);
        this.logger.info("- START: " + this);

        // Ask prices of each product to sellers. The ask price behaviour choses the
        // seller with which to negotiate
        // The ask price behaviour will start the negotiation with the chosen seller.
        for (Map.Entry<Product, List<ProductStatus>> p : this.products.entrySet())
            for(int i = 0; i < p.getValue().size(); i++)
                this.addBehaviour(new NegotiateBuyer(p.getKey(), i, this, new ACLMessage(ACLMessage.CFP)));
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

    public SequentialBehaviour getBehaviour() {
        return this.negotiationsBehaviour;
    }

    @Override
    public String toString() {
        if (this.getLocalName() != null)
            return this.getLocalName() + "{" + "products=" + this.productsToString() + "}";
        return "Buyer{" + "products=" + this.productsToString() + "}";
    }
    /**
     * buyer_0{products={2-pc:650.00=[TRYING, TRYING], ...}}
     */
    public String productsToString() {
        StringBuilder res = new StringBuilder("{");
        this.products.entrySet().forEach(entry-> res.append(entry.getValue().size() + "-" + entry.getKey() + "=" + entry.getValue() + ", ") );
        String fres = res.toString();
        if(fres.substring(fres.length() - 1).equals(" "))
            return fres.substring(0, fres.length() - 2) + "}";
        return fres + "}";
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

    public void noSellerForProduct(Product product) {
        List<ProductStatus> status = this.products.get(product);
        for(int i = 0; i < status.size(); i++)
            if(status.get(i) == ProductStatus.TRYING)
                status.set(i, ProductStatus.NO_SELLER); 
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

        if(OlxNetwork.DISPLAY_NET)
            this.node.clearOutEdges();
        for (Handler h: this.logger.getHandlers())
            h.close();

        terminationListener.terminated(this);
    }

    public boolean isBuying(Product product, int index) {
        List<ProductStatus> status = this.products.get(product);
        return status.get(index) == ProductStatus.TRYING;
    }

    @JsonIgnore(true)
    public void setNode(DefaultDrawableNode node) {
        this.node = node;
    }

    public void removeLastEdgeIfApplicable(DefaultDrawableNode node) {

        if(Olx.SHOWN_EDGE_COUNT == -1)
            return;

        if(this.lastEdges.size() > Olx.SHOWN_EDGE_COUNT){
            Edge toRemove = this.lastEdges.pop();
            node.removeOutEdge(toRemove);
        }
    }

    public void storeEdge(Edge newEdge) {
        if(Olx.SHOWN_EDGE_COUNT == -1)
            return;

        this.lastEdges.add(newEdge);
    }
}
