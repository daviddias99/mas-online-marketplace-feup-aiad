package agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import agents.strategies.NaivePickingStrategy;
import behaviours.AskPriceBuyer;
import models.Product;

public class Buyer extends Agent {
    // TODO: depois por lista de produtos (??)/received
    private Map<Product, Boolean> products = new HashMap<>();

    public Buyer(List<String> products) {
        for (int i = 0; i < products.size(); i++)
            this.products.put(new Product(products.get(i)), false);
    }

    public Set<Product> getProducts() {
        return this.products.keySet();
    }

    public Set<Product> getMissingProducts() {
        return (this.products.entrySet().stream().filter(map -> !map.getValue())
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()))).keySet();
    }

    @Override
    public String toString() {
        String result = this.getName() + ":\n";
        for (Product p : this.products.keySet())
            result += "  - " + p.toString() + ":" + this.products.get(p).toString() + "\n";
        return result;
    }
    
    @Override
    protected void setup() {
        // TODO: depois ver se dá para mudar para um que repita ciclicamente até success
        // true
        // se calhar dá para chegar ao final e por reset se success false

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return;
        }
        System.out.println("Agent " + this.getAID() + " slept.");

        for (Product p : this.products.keySet()){
            System.out.printf(" - START: Agent %s - Product %s%n", this.getLocalName(), p.getName());
            SequentialBehaviour seq = new SequentialBehaviour();
            seq.addSubBehaviour(new AskPriceBuyer(p, this, new ACLMessage(ACLMessage.REQUEST), new NaivePickingStrategy()));
            addBehaviour(seq);
        }

    }
}
