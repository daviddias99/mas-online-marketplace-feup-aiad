package src.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import src.agents.Seller;
import src.models.Product;
import src.models.SellerOfferInfo;

public class AskPriceSeller extends AskPrice {

    public AskPriceSeller(Product product, Seller seller, ACLMessage msg) {
        super(product, seller, msg);
    }

    @Override
    protected void handleNoResults() {
        System.out.println(" - NONE FOUND: no seller found for " + this.getProduct().getName() + ",  " + this.getAgent().getLocalName());
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.removeProduct(p);
        p.setMarketPrice(this.calculateInitialPrice());
        s.addProduct(p);

        s.register(p);
        // s.addBehaviour(new ResponsePrice(s,
        // MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
    }

    private int calculateInitialPrice() {
        // TODO: improve this function
        return (int) (((Seller) this.getAgent()).getCredibility() / 100.0 * this.getProduct().getOriginalPrice());
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
        System.out.println("Calculating price for " + this.getAgent().getLocalName());
        // TODO: refactor pq é igual a cima para já (??)
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.removeProduct(p);
        p.setMarketPrice(this.calculateInitialPrice());
        s.addProduct(p);

        s.register(p);
    }
}
