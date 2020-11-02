package agents;

import behaviours.AskPriceSeller;
import behaviours.ResponsePrice;
import models.Product;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
        for(Entry<String, Integer> product : productMap.entrySet())
            this.products.add(new Product(product.getKey(), product.getValue()));
    }

    public int getCredibility(){
        return this.credibility;
    }

    public void addProduct(String name, int originalPrice){
        this.products.add(new Product(name, originalPrice));
    }

    public void addProduct(Product product){
        this.products.add(product);
    }

    public Set<Product> getProducts(){
        return this.products;
    }

    public boolean removeProduct(Product product){
        return this.products.remove(product);
    }

    public Product getProduct(String name){
        Product search = new Product(name);
        if (this.products.contains(search))
            for (Product p : this.products)
                if (search.equals(p))
                    return p;
        return null;
    }

    @Override
    protected void setup() {
        // register();
        // TODO: ver depois sequencia
        for (Product p : this.products)
            addBehaviour(new AskPriceSeller(p, this, new ACLMessage(ACLMessage.REQUEST)));
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
    }

    @Override
    protected void takeDown() {
        deregister();
    }

    public void register(Product product) {
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType(product.getName());
        dfd.addServices(sd);

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
