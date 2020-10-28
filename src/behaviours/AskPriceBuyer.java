package src.behaviours;

import java.util.Vector;
import java.util.HashMap;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import src.agents.Buyer;
import src.agents.Seller;
import src.agents.strategies.SellerPickingStrategy;
import src.models.Product;
import src.models.SellerOfferInfo;

public class AskPriceBuyer extends AskPrice {

    private SellerPickingStrategy sellerPickingStrategy;

    public AskPriceBuyer(Product product, Buyer buyer, ACLMessage msg, SellerPickingStrategy sellerPickingStrategy) {
        super(product, buyer, msg);
        this.sellerPickingStrategy = sellerPickingStrategy;
    }

    @Override
    protected void handleNoResults() {
        System.out.printf("// TODO: There was no product for buyer %s searching for %s%n", this.getAgent().getLocalName(), this.getProduct());
    }

    @Override
    protected void handleAllResultNotifications(Vector responses){

        System.out.printf("> %s got %d responses!%n", this.getAgent().getLocalName(),responses.size());

        HashMap<AID,SellerOfferInfo> offers = new HashMap<>();
        
        Vector<ACLMessage> convertedResponses = responses;

        for(ACLMessage msg : convertedResponses){
            
            if(msg.getPerformative() != ACLMessage.INFORM){
                System.out.printf("> Message from %s indicating a failure.%n",msg.getSender().getLocalName());
                continue;
            }
            try {
                SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                offers.put(msg.getSender(), sellerOffer);
            } catch (UnreadableException e) {
                System.out.printf("> Message from %s contained invalid content.",msg.getSender().getLocalName());
            }
        }

        AID chosenSeller = this.sellerPickingStrategy.pickSeller(offers);
        System.out.printf("> Picked seller %s%n", chosenSeller.getLocalName());
    }

    // // TODO: escolher entre handleAllResultNotifications (analisar todos de uma vez
    // // no final)
    // // e como o professor tem
    // protected void handleInform(ACLMessage inform) {
    //     try {
    //         Product productReponse = (Product)inform.getContentObject();
    //         System.out.printf(" < RECEIVED: %s with %s from %s\n", this.getAgent().getLocalName(), productReponse, inform.getSender().getLocalName());
    //     } catch (UnreadableException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }

    // protected void handleFailure(ACLMessage failure) {
    //     System.out.println(failure);
    // }
    
}
