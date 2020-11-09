package agents;

import behaviours.AskPriceSeller;
import behaviours.ResponsePrice;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Product;

import java.util.HashMap;
import java.util.Map;
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
    
    private int credibility;
    private DFAgentDescription dfd;
    
    // True if this is the first time the seller is registering a product
    private boolean firstTime = true;

    @JsonCreator
    public Seller(@JsonProperty("products") String[] products, @JsonProperty("credibility") int credibility) {
        this.credibility = credibility;
        for (int i = 0; i < products.length; i++)
            this.products.put(new Product(products[i]), 0.0f);
    }

    @Override
    protected void setup() {
        // Agent registration object inside the DF registry. An agent provides one service
        // for each object he his selling.
        this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID());

        // Query at what prices the other agents are selling the producs in order to decide
        // selling price.
        for (Product p : this.products.keySet())
            addBehaviour(new AskPriceSeller(p, this, new ACLMessage(ACLMessage.REQUEST)));

        // Listen for buyer queries about sellling price
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
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

        // Sujo mas funciona. Melhor era qd se fizesse this.dfd.getAllServices() o
        // iterator funcionar para ver se havia ou não, mas estava me sempre a dar true

        try {
            if (!this.firstTime) {
                DFService.modify(this, this.dfd);
            } else {
                DFService.register(this, this.dfd);
                this.firstTime = false;
            }

        } catch (FIPAException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Assim ficaria mais clean que em cima, mas por alguma razão ele n apanha o
        // Warning do Already-Register (dá certo, mas dá print do warning)
        // Warning apenas aparece qd não se liga GUI atenção
        /*
         * try { DFService.register(this, this.dfd); } catch (FIPAException e) { try {
         * DFService.modify(this, this.dfd); } catch (FIPAException e1) { // TODO
         * Auto-generated catch block e.printStackTrace(); e1.printStackTrace(); }
         * 
         * }
         */
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
            return this.getLocalName() + "{credibility=" + credibility + ", products=" + products + '}';    
        return "Seller{credibility=" + credibility + ", products=" + products + '}';
    }
}
