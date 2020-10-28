package src.behaviours;

import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import src.agents.Seller;
import src.models.Product;

public class AskPriceSeller extends AskPrice {

    public AskPriceSeller(Product product, Seller seller, ACLMessage msg) {
        super(product, seller, msg);
    }

    @Override
    protected void handleNoResults() {
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.removeProduct(p);
        p.setMarketPrice(this.calculateInitialPrice());
        s.addProduct(p);

        s.register(p);
        // s.addBehaviour(new ResponsePrice(s, MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
    }

    private int calculateInitialPrice(){
        // TODO: improve this function
        return ((Seller) this.getAgent()).getCredibility() * this.getProduct().getOriginalPrice();
    }

    protected void handleAllResultNotifications(Vector<ACLMessage> resultNotifications){
        
    }

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
