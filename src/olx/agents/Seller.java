package olx.agents;

import olx.behaviours.AskPriceSeller;
import olx.behaviours.NegotiationDispatcher;
import olx.behaviours.ResponsePrice;
import olx.draw.NetworkAgent;
import olx.draw.OlxNetwork;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import olx.Olx;
import olx.agents.strategies.offer.*;
import olx.agents.strategies.price_picking.*;
import olx.models.Product;
import olx.models.Stock;
import uchicago.src.sim.network.DefaultDrawableNode;
import olx.utils.CoolFormatter;
import olx.utils.ProductQuantity;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sajas.core.Agent;
import sajas.core.behaviours.ParallelBehaviour;
import sajas.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import olx.utils.Util;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Seller extends Agent implements NetworkAgent {

    // List of products which the seller is currently offering and the price
    // of said products (float)
    private Map<Product, Stock> products = new ConcurrentHashMap<>();

    private int credibility; // 0 to 100
    private final int scamFactor; // 0 to 100
    private final int elasticity; // 0 to 100, but normally smaller than 20
    private DFAgentDescription dfd;
    private OfferStrategy offerStrategy;
    private PricePickingStrategy pricePickingStrategy;
    private float wealth;
    private transient Logger logger;
    private DefaultDrawableNode node;

    @JsonCreator
    public Seller(@JsonProperty("products") ProductQuantity[] products, @JsonProperty("scamFactor") int scamF,
                  @JsonProperty("elasticity") int elasticity, @JsonProperty("pickingStrategy") String pickingStrategy,
                  @JsonProperty("offerStrategy") String offerStrategy) {
        if (scamF > 100 || scamF < 0)
            throw new IllegalArgumentException("Scam Factor must be from 0 to 100 and was " + scamF);
        if (elasticity > 100 || elasticity < 0)
            throw new IllegalArgumentException("Elasticity must be from 0 to 100 and was " + elasticity);

        this.pricePickingStrategy = PickingStrategyFactory.get(pickingStrategy);
        this.offerStrategy = OfferStrategyFactory.get(offerStrategy);

        this.scamFactor = scamF;
        this.elasticity = elasticity;
        this.credibility = 100;

        do {
            this.credibility = Util.randomBetween(50, 100);
        } while (this.credibility > 100);

        for (int i = 0; i < products.length; i++)
            this.products.put(products[i].getProduct(), new Stock(0.0f, products[i].getQuantity()));

        this.wealth = 0;
    }

    public Logger logger() {
        return this.logger;
    }

    @Override
    protected void setup() {
        this.setupLogger();
        if(!Olx.logging) this.logger.setLevel(Level.OFF);
        this.logger.info("- START: " + this);
        // Agent registration object inside the DF registry. An agent provides one service
        // for each object he his selling.
        this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID());
        try {
            DFService.register(this, this.dfd);
        } catch (FIPAException e) {
            this.logger.warning(String.format("/!\\ %s could not register itself in the DF service%n", this.getLocalName()));
            System.exit(-1);
        }

        // Query at what prices the other olx.agents are selling the producs in order to decide
        // selling price.

        ParallelBehaviour askPricesToSellersBehavior = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        for (Product p : this.products.keySet())
            askPricesToSellersBehavior.addSubBehaviour(new AskPriceSeller(p, this, new ACLMessage(ACLMessage.REQUEST)));

        addBehaviour(askPricesToSellersBehavior);
        // Listen for other seller queries about selling price
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        // Listen for buyer queries about selling price and negotiating
        addBehaviour(new NegotiationDispatcher(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }

    @Override
    protected void takeDown() {
        deregister();
        this.logger().info(String.format("! %s is leaving%n", this.getLocalName()));

        for(Handler h: this.logger.getHandlers())
            h.close();

        System.out.printf("! %s is leaving %n", this.getLocalName());
    }

    public void register(Product product) {

        try {

            DFAgentDescription dfdCopy = this.makeCopy(this.dfd);
            DFService.deregister(this, this.dfd);
            this.dfd = dfdCopy;

            // Register product under this agent
            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType(product.getName());
            this.dfd.addServices(sd);

            DFService.register(this, this.dfd);

        } catch (FIPAException e1) {
            this.logger.warning(String.format("/!\\ %s could not register product %s in the DF service%n", this.getLocalName(), product));
        }
    }

    public DFAgentDescription makeCopy(DFAgentDescription dfdesc){
        DFAgentDescription result = new DFAgentDescription();
        result.setName(getAID());

        Iterator<ServiceDescription> it = dfdesc.getAllServices();
        while (it.hasNext()) {
            ServiceDescription s = it.next();
            result.addServices(s);
        }
        return result;
    }

    public void deregister(Product product) {
        
        try {
            DFAgentDescription dfdCopy = this.makeCopy(this.dfd);
            DFService.deregister(this, this.dfd);
            this.dfd = dfdCopy;

            Iterator<ServiceDescription> it = this.dfd.getAllServices();

            while (it.hasNext()) {
                ServiceDescription sd = it.next();

                if (sd.getType().equals(product.getName())) {
                    this.dfd.removeServices(sd);
                    break;
                }
            }

            DFService.register(this, this.dfd);
        } catch (FIPAException e1) {
            this.logger.warning(String.format("/!\\ %s could not remove product %s from the DF service%n", this.getLocalName(), product));
        }
    }

    private void deregister() {
        try {
            DFAgentDescription dfdCopy = this.makeCopy(this.dfd);
            DFService.deregister(this);
            this.dfd = dfdCopy;
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public int reduceCredibility(){
        int reduceFactor = Util.randomBetween(70, 90);
        this.credibility = reduceFactor * this.credibility / 100;

        if(this.node != null)
            this.updateNodeColor();
        return this.credibility;
    }


    public int increaseCredibility(){
        int increaseFactor = Util.randomBetween(110, 140);
        this.credibility = Math.min(100, increaseFactor * this.credibility / 100);

        if(this.node != null)
            this.updateNodeColor();
        return this.credibility;
    }

    //
    // Helper
    //

    public PricePickingStrategy getPricePickingStrategy() {
        return pricePickingStrategy;
    }

    public OfferStrategy getOfferStrategy() {
        return offerStrategy;
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
            System.out.printf("/!\\ seller %s was unable to setup logger%n", this.getLocalName());
        }
    }

    public int getElasticity(){
        return this.elasticity;
    }
    
    public int getCredibility() {
        return this.credibility;
    }

    public int getScamFactor() {
        return this.scamFactor;
    }
    
    public boolean hasProduct(Product product) {
        return this.products.containsKey(product);
    }

    public Set<Product> getProducts() {
        return this.products.keySet();
    }

    public synchronized Stock removeProduct(Product product) {
        Stock stock = this.products.get(product);
        if(stock == null) return stock;
        stock.decreaseQuantity();
        if(stock.empty()) return this.products.remove(product);
        return stock;
    }

    public Product getProduct(String name) {
        Product search = new Product(name);
        if (this.products.get(search) != null)
            for (Product p : this.products.keySet())
                if (search.equals(p))
                    return p;
        return null;
    }

    public Float getProductPrice(String name) {
        return this.products.get(this.getProduct(name)).getPrice();
    }

    public int getProductQuantity(String name) {
        return this.products.get(this.getProduct(name)).getQuantity();
    }

    public Stock getProductStock(Product product){
        return this.products.get(product);
    }

    @Override
    public String toString() {
        if (this.getLocalName() != null)
            return this.getLocalName() + "{credibility:scamF=" + credibility + ":" + this.scamFactor + ", elasticity=" + this.elasticity + ", products=" + products + '}';
        return "Seller{credibility:scamF=" + credibility + ":" + this.scamFactor + ", elasticity=" + this.elasticity + ", products=" + products + '}';
    }

    public synchronized void changeWealth(float variance) {
        this.wealth += variance;
    }

    public float getWealth(){
        return this.wealth;
    }

    public boolean doScam() {
        return Util.randomBetween(0, 100) < scamFactor;
    }

    public boolean finished() {
        return false;
    }

    @JsonIgnore()
    public void setNode(DefaultDrawableNode node) {
        this.node = node;
    }

    public DefaultDrawableNode getNode() {
        return this.node;
    }

    private void updateNodeColor() {
        this.node.setColor(Util.getSellerColor(this.credibility));
        this.node.setBorderColor(Util.getSellerColor(50));
    }
}
