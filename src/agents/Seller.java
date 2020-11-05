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
import jade.util.leap.Iterator;

public class Seller extends Agent {
    // TODO: assim ou String to Product pra acesso mais rápido
    private Map<Product,Float> products = new HashMap<>();
    private int credibility;
    private DFAgentDescription dfd;
    private boolean firstTime = true;

    // TODO: ver como queremos dar input dos products
    @JsonCreator
    public Seller(@JsonProperty("products") Product[] products, @JsonProperty("credibility") int credibility) {
        this.credibility = credibility;
        for (Product p : products)
            this.products.put(p, 0.0f);

    }

    public int getCredibility(){
        return this.credibility;
    }

    public void addProduct(String name, int originalPrice){
        this.products.put(new Product(name, originalPrice),0.0f);
    }

    public void addProduct(Product product){
        this.products.put(product,0.0f);
    }

    public void addProduct(Product product, float marketPrice){
        this.products.put(product, marketPrice);
    }

    public Set<Product> getProducts(){
        return this.products.keySet();
    }

    public float removeProduct(Product product){
        return this.products.remove(product);
    }

    public Product getProduct(String name){
        Product search = new Product(name);
        if (this.products.get(search) != null)
            for (Product p : this.products.keySet())
                if (search.equals(p))
                    return p;
        return null;
    }

    public Float getProductPrice(String name){
        return this.products.get(this.getProduct(name)); 
    }

    @Override
    protected void setup() {
        // register();
        // TODO: ver depois sequencia
        this.dfd = new DFAgentDescription();
        this.dfd.setName(getAID());
        for (Product p : this.products.keySet())
            addBehaviour(new AskPriceSeller(p, this, new ACLMessage(ACLMessage.REQUEST)));
        addBehaviour(new ResponsePrice(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
    }

    @Override
    protected void takeDown() {
        deregister();
    }

    public void register(Product product) {
        
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType(product.getName());
        this.dfd.addServices(sd);
        
        // Sujo mas funciona. Melhor era qd se fizesse this.dfd.getAllServices() o iterator funcionar para ver se havia ou não, mas estava me sempre a dar true
        if(!this.firstTime)
            try {
                DFService.modify(this, this.dfd);
            } catch (FIPAException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        else
            try {
                DFService.register(this, this.dfd);
                this.firstTime = false;
            } catch (FIPAException e) {
                e.printStackTrace();
            }

        // Assim ficaria mais clean que em cima, mas por alguma razão ele n apanha o Warning do Already-Register (dá certo, mas dá print do warning)
        // Warning apenas aparece qd não se liga GUI atenção
        /*
        try {
            DFService.register(this, this.dfd);
        } catch (FIPAException e) {
            try {
                DFService.modify(this, this.dfd);
            } catch (FIPAException e1) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                e1.printStackTrace();
            }

        }
        */
    }

    private void deregister() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Seller{" +
                "products=" + products +
                ", credibility=" + credibility +
                '}';
    }
}
