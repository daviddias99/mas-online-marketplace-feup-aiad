package behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import agents.Seller;
import models.Product;
import models.SellerOfferInfo;

public class AskPriceSeller extends AskPrice {

    public AskPriceSeller(Product product, Seller seller, ACLMessage msg) {
        super(product, seller, msg);
    }

    @Override
    protected void handleNoResults() {
        System.out.println(" - NONE FOUND: no seller found for " + this.getProduct().getName() + ",  " + this.getAgent().getLocalName());
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.addProduct(p, this.calculateInitialPrice(s, p));

        s.register(p);
        // s.addBehaviour(new ResponsePrice(s, MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
    }

    private float calculateInitialPrice(Seller s, Product p) {
        // TODO: improve this function
        return (float) (s.getCredibility() / 100.0 * p.getOriginalPrice());
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        List<SellerOfferInfo> marketPrices = new ArrayList<>();

        for (int i = 0; i < resultNotifications.size(); i++) {
            ACLMessage message = (ACLMessage) resultNotifications.get(i);
            try {
                marketPrices.add((SellerOfferInfo) message.getContentObject());
            } catch (UnreadableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.printf("Product %s has %d sellers with these prices:%n", this.getProduct().getName(), marketPrices.size());
        for(SellerOfferInfo p: marketPrices)
            System.out.printf(" - %f%n", p.getOfferedPrice());
            
        // TODO: implement one function
        // TODO: refactor pq é igual a cima para já (??)
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        System.out.println("Calculating price of " + p.getName() + " for " + s.getLocalName());
        s.addProduct(p, this.calculateInitialPrice(s, p));
        s.register(p);
    }
}
