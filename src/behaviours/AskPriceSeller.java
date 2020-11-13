package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import models.Product;
import models.SellerOfferInfo;
import agents.Seller;
import jade.core.AID;
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

    @Override
    public Seller getAgent(){
        return (Seller) super.getAgent();
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

            // Logging
            StringBuilder sb = new StringBuilder(String.format("> %s asked the price of *%s* to the following sellers:", this.getAgent().getLocalName(), this.getProduct().getName()));
            Iterator<AID> it = msg.getAllReceiver();
            while(it.hasNext())
                sb.append(String.format("%n - %s", it.next().getLocalName()));
            this.getAgent().logger.info(sb.toString());

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
        Seller s = this.getAgent();
        Product p = this.getProduct();
        s.addProduct(p, this.calculateInitialPrice(s, p));
        s.logger.info(String.format("! %s found no sellers for product %s. Setting price at %.2f", s.getLocalName(), p.getName(), s.getProductPrice(this.getProduct().getName())));
        // Register that agent is selling <product> in the DF registry
        s.register(p);
    }

    private float calculateInitialPrice(Seller s, Product p) {
        // TODO: improve this function
        return (float) (s.getCredibility() / 100.0 * p.getOriginalPrice());
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        Seller s = this.getAgent();
        Product p = this.getProduct();

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

        StringBuilder sb = new StringBuilder(String.format("< %s found that product %s has %d sellers with these prices:", s.getLocalName(), p.getName(), marketPrices.size()));
        for(SellerOfferInfo soInfo: marketPrices)
            sb.append(String.format("%n - %.2f", soInfo.getOfferedPrice()));
        s.logger.info(sb.toString());
            
        // TODO: implement one function
        // TODO: refactor pq é igual a cima para já (??)        
        // Other sellers are currenttly selling <product>
        // set selling price accordingly
        s.addProduct(p, this.calculateInitialPrice(s, p));
        s.logger.info(String.format("! %s set product %s price at %.2f", s.getLocalName(), p.getName(), s.getProductPrice(p.getName())));
        s.register(p);
    }
    // TODO: ver se vale a pena handlers da 1st part
}
