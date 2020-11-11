package agents;

import behaviours.AskPriceSeller;
import behaviours.NegotiationDispatcher;
import behaviours.ResponsePrice;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import models.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Seller extends Agent {
    // TODO: assim ou String to Product pra acesso mais rápido

    // List of products which the seller is currently offering and the price
    // of said products (float)
    private Map<Product, Float> products = new HashMap<>();
    
    private int credibility; // 0 to 100
    private final int scamFactor; // 0 to 100
    private final int elasticity; // 0 to 100, but normally smaller than 20
    private DFAgentDescription dfd;
    
    @JsonCreator
    public Seller(@JsonProperty("products") Product[] products, @JsonProperty("scamFactor") int scamF, @JsonProperty("elasticity") int elasticity) {
        if(scamF > 100 || scamF < 0)
            throw new IllegalArgumentException("Scam Factor must be from 0 to 100 and was " + scamF);
        if(elasticity > 100 || elasticity < 0)
            throw new IllegalArgumentException("Elasticity must be from 0 to 100 and was " + elasticity);
        
        this.scamFactor = scamF;
        this.elasticity = elasticity;
        // TODO: brincar com isto
        // * Std Deviation + Mean
        do{
            this.credibility = (int) Math.abs((new Random()).nextGaussian() * (elasticity / 2) + scamF);
        } while(this.credibility > 100);

        for (int i = 0; i < products.length; i++)
            this.products.put(products[i], 0.0f);
    }

    @Override
    protected void setup() {
        // Agent registration object inside the DF registry. An agent provides one service
        // for each object he his selling.
        this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID());
        try {
            DFService.register(this, this.dfd);
        } catch (FIPAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Query at what prices the other agents are selling the producs in order to decide
        // selling price.
        for (Product p : this.products.keySet())
            addBehaviour(new AskPriceSeller(p, this, new ACLMessage(ACLMessage.REQUEST)));

        // Listen for other seller queries about selling price
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        // Listen for buyer queries about selling price and negotiating
        addBehaviour(new NegotiationDispatcher(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }

    @Override
    protected void takeDown() {
        deregister();
    }

    public void register(Product product) {

        // Register product under this agent
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType(product.getName());
        this.dfd.addServices(sd);

        try {
            DFService.modify(this, this.dfd);
        } catch (FIPAException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private void deregister() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    //
    // Helper
    //

    public int getCredibility() {
        return this.credibility;
    }

    public void addProduct(String name, int originalPrice) {
        this.products.put(new Product(name, originalPrice), 0.0f);
    }

    public void addProduct(Product product) {
        this.products.put(product, 0.0f);
    }

    public void addProduct(Product product, float marketPrice) {
        this.products.put(product, marketPrice);
    }

    public boolean hasProduct(Product product){
        return this.products.containsKey(product);
    }

    public Set<Product> getProducts() {
        return this.products.keySet();
    }

    public void setProducts(Map<Product, Float> newP){
        this.products = newP;
    }

    public float removeProduct(Product product) {
        return this.products.remove(product);
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
        return this.products.get(this.getProduct(name));
    }

    @Override
    public String toString() {
        if(this.getLocalName() != null)
            return this.getLocalName() + "{credibility:scamF=" + credibility + ":" + this.scamFactor + ", elasticity=" + this.elasticity + ", products=" + products + '}';    
        return "Seller{credibility:scamF=" + credibility + ":" + this.scamFactor + ", elasticity=" + this.elasticity + ", products=" + products + '}';
    }
}
