package agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import agents.counterOfferStrategies.NaiveCounterOfferStrategy;
import agents.filteringStrategies.NaiveFilterStrategy;
import behaviours.NegotiateBuyer;
import models.Product;

public class Buyer extends Agent {
    // TODO: depois por lista de produtos (??)/received

    // The products map contains pairs where the values are true if the
    // buyer as acquired the key product.
    private Map<Product, Boolean> products = new HashMap<>();

    @JsonCreator
    public Buyer(@JsonProperty("products") String[] products) {
        for (int i = 0; i < products.length; i++)
            this.products.put(new Product(products[i]), false);
    }

    // The buyer cyrrentlt
    @Override
    protected void setup() {
        // TODO: depois ver se dá para mudar para um que repita ciclicamente até success true. 
        // Se calhar dá para chegar ao final e por reset se success false

        // Buyers sleep to allow for seller setup
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        // Ask prices of each product to sellers. The ask price behaviour choses the seller with which to negotiate
        // The ask price behaviour will start the negotiation with the chosen seller.
        for (Product p : this.products.keySet()) {
            System.out.printf(" - START: Agent %s - Product %s%n", this.getLocalName(), p.getName());
            addBehaviour(new NegotiateBuyer(p, this, 
                new ACLMessage(ACLMessage.CFP), 
                new NaiveFilterStrategy(), 
                new NaiveCounterOfferStrategy()
                ));
        }

    }

    //
    // Helpers
    //

    public Set<Product> getProducts() {
        return this.products.keySet();
    }

    // Get proiducts that have yet to be bough by the buyer
    public Set<Product> getMissingProducts() {
        return (this.products.entrySet().stream().filter(map -> !map.getValue())
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()))).keySet();
    }

    // Example:
    // buyer_0:
    // - pc : 150 : false
    // - skate : 100 : true
    @Override
    public String toString() {
        if(this.getLocalName() != null)
            return this.getLocalName() + "{" + "products=" + this.products + "}";
        return "Buyer{" + "products=" + this.products + "}";
    }

}
