package src.agents;

import src.behaviours.ResponsePrice;
import src.models.Product;

import java.util.HashSet;
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
    private Set<Product> products = new HashSet<>();
    private int credibility;

    // TODO: ver como queremos dar input dos products
    public Seller(Map<String, Integer> productMap, int credibility) {
        this.credibility = credibility;
        for(String productName : productMap.keySet())
            this.products.add(new Product(productName, productMap.get(productName)));
    }

    public void addProduct(String name, int originalPrice){
        this.products.add(new Product(name, originalPrice));
    }

    public Set<Product> getProducts(){
        return this.products;
    }

    public Product getProduct(String name){
        Product search = new Product(name);
        if (this.products.contains(search))
            for (Product p : this.products)
                if (search.equals(p))
                    return p;
        return null;
    }

    protected void setup() {
        register();
        // TODO: ver depois isto pq deve acabar e depois nunca mais
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
    }

    protected void takeDown() {
        deregister();
    }

    private void register() {
        

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        for(Product product : this.products){
            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType(product.getName());
            dfd.addServices(sd);
        }

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void deregister() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
}
