package agents;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import agents.strategies.counter_offer.*;
import behaviours.NegotiateBuyer;
import models.Product;
import utils.CoolFormatter;

public class Buyer extends Agent {

    // The products map contains pairs where the values are true if the
    // buyer as acquired the key product.
    private Map<Product, ProductStatus> products = new ConcurrentHashMap<>();
    private CounterOfferStrategy counterOfferStrategy;
    private float wealth;
    private int patience;
    private Set<AID> blackList;
    private transient Logger logger;
    private ParallelBehaviour negotiationsBehaviour;
    private boolean kill;

    enum ProductStatus {
        TRYING, BOUGHT, NO_SELLER,
    }

    @JsonCreator
    public Buyer(@JsonProperty("products") String[] products,
            @JsonProperty("counterOfferStrategy") String counterOfferStrategy, @JsonProperty("patience") int patience) {
        for (int i = 0; i < products.length; i++)
            this.products.put(new Product(products[i]), ProductStatus.TRYING);
        if (patience > 100 || patience < 0)
            throw new IllegalArgumentException("Patience must be from 0 to 100 and was " + patience);
        this.kill = false;
        this.wealth = 0;
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
        for (Product p : this.products.keySet())
            negotiationsBehaviour.addSubBehaviour(new NegotiateBuyer(p, this, new ACLMessage(ACLMessage.CFP)));

        this.addBehaviour(negotiationsBehaviour);
    }

    public boolean finished() {

        for (Map.Entry<Product, ProductStatus> p : this.products.entrySet()) {
            if (p.getValue() == ProductStatus.TRYING) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void takeDown() {
        System.out.println(this.getLocalName() + " is the impostor.");
        for (Handler h : this.logger.getHandlers())
            h.close();

        if (this.kill) {
            AMSAgentDescription[] agents = null;
            try {
                SearchConstraints c = new SearchConstraints();
                agents = AMSService.search(this, new AMSAgentDescription());
            } catch (Exception e) {
                // TODO
            }

            int numBuyers = 0;
            for (AMSAgentDescription agentDescription : agents) {
                if (agentDescription.getName().toString().contains("buyer")
                        && !agentDescription.getName().equals(this.getAID())) {
                    numBuyers++;
                }
            }

            if (numBuyers == 0) {
                Codec codec = new SLCodec();
                Ontology jmo = JADEManagementOntology.getInstance();
                getContentManager().registerLanguage(codec);
                getContentManager().registerOntology(jmo);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAMS());
                msg.setLanguage(codec.getName());
                msg.setOntology(jmo.getName());
                try {
                    getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
                    send(msg);
                } catch (Exception e) {
                    // todo
                }
            }
        }
    }

    //
    // Helpers
    //

    public void setKillIfLast(boolean kill) {
        this.kill = kill;
    }

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

    // Get proiducts that have yet to be bough by the buyer
    public Set<Product> getMissingProducts() {
        return (this.products.entrySet().stream().filter(map -> map.getValue() == ProductStatus.TRYING)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))).keySet();
    }

    @Override
    public String toString() {
        if (this.getLocalName() != null)
            return this.getLocalName() + "{" + "products=" + this.products + "}";
        return "Buyer{" + "products=" + this.products + "}";
    }

    public synchronized void changeWealth(float variance) {
        this.wealth += variance;
    }

    public void receivedProduct(Product product) {
        this.products.put(product, ProductStatus.BOUGHT);
    }

    public void noSellerForProduct(Product product) {
        this.products.put(product, ProductStatus.NO_SELLER);
    }

    public boolean isBuying(Product product) {
        return this.products.get(product) == ProductStatus.TRYING;
    }
}
