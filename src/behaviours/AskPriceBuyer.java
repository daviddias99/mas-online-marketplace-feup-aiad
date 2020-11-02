package behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import agents.Buyer;
import models.Product;

public class AskPriceBuyer extends AskPrice {

    public AskPriceBuyer(Product product, Buyer buyer, ACLMessage msg) {
        super(product, buyer, msg);
    }

    @Override
    protected void handleNoResults() {
        System.out.printf("// TODO: There was no product for buyer %s searching for %s%n", this.getAgent().getLocalName(), this.getProduct());
    }

    // TODO: escolher entre handleAllResultNotifications (analisar todos de uma vez
    // no final)
    // e como o professor tem
    @Override
    protected void handleInform(ACLMessage inform) {
        try {
            Product productReponse = (Product)inform.getContentObject();
            System.out.printf(" < RECEIVED: %s with %s from %s%n", this.getAgent().getLocalName(), productReponse, inform.getSender().getLocalName());
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        System.out.println(failure);
    }
    
}
