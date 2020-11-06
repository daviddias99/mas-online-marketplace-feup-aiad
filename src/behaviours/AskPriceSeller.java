package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import models.Product;
import models.SellerOfferInfo;
import agents.Seller;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class AskPriceSeller extends AchieveREInitiator {

    private Product product;

    /**
     * Seller <agent> is asking the selling price of <product>
     */
    public AskPriceSeller(Product product, Seller agent, ACLMessage msg) {
        super(agent, msg);
        this.product = product;
    }

    protected Product getProduct(){
        return this.product;
    }
    
    @Override
    protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
        Vector<ACLMessage> v = new Vector<ACLMessage>();

        // Query df service for agents who are selling <product>
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);
            
            // No agents are selling <product>
            if(result.length == 0){
                this.handleNoResults();
                return v;
            }

            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        // The <product> is sent as the content so that the 
        // seller knows to which product the request pertains to
        try {
            msg.setContentObject(this.product);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return v;
        }

        v.add(msg);

        return v;
    }

    protected void handleNoResults() {

        // No other sellers are currenttly selling <product>
        // set selling price accordingly
        System.out.println(" - NONE FOUND: no seller found for " + this.getProduct().getName() + ",  " + this.getAgent().getLocalName());
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.addProduct(p, this.calculateInitialPrice(s, p));
        
        // Register that agent is selling <product> in the DF registry
        s.register(p);
    }

    private float calculateInitialPrice(Seller s, Product p) {
        // TODO: improve this function
        return (float) (s.getCredibility() / 100.0 * p.getOriginalPrice());
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        List<SellerOfferInfo> marketPrices = new ArrayList<>();

        // Collect current market prices
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

        // Other sellers are currenttly selling <product>
        // set selling price accordingly
        s.addProduct(p, this.calculateInitialPrice(s, p));
        s.register(p);
    }
    // TODO: ver se vale a pena handlers da 1st part
}
