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
    private Seller seller;

    /**
     * Seller <agent> is asking the selling price of <product>
     */
    public AskPriceSeller(Product product, Seller agent, ACLMessage msg) {
        super(agent, msg);
        this.product = product;
        this.seller = agent;
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

            System.out.printf("> %s asked the price of %s to the following sellers:%n", this.getAgent().getLocalName(), this.getProduct().getName());

            Iterator<AID> it = msg.getAllReceiver();
            
            while(it.hasNext()){
                System.out.printf(" - %s%n", it.next().getLocalName());
            }

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
        System.out.printf("! %s found no sellers selling product %s%n", this.getAgent().getLocalName(), this.getProduct().getName());
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        s.addProduct(p,this.seller.getPricePickingStrategy().calculateInitialPrice(s, p));
        System.out.printf("! %s set product %s price at %f%n", this.getAgent().getLocalName(), this.getProduct().getName(), s.getProductPrice(this.getProduct().getName()));
        // Register that agent is selling <product> in the DF registry
        s.register(p);
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

        System.out.printf("> %s found that product %s has %d sellers with these prices:%n",this.getAgent().getLocalName(), this.getProduct().getName(), marketPrices.size());
        for(SellerOfferInfo p: marketPrices)
            System.out.printf(" - %f%n", p.getOfferedPrice());
            
        // TODO: implement one function
        // TODO: refactor pq é igual a cima para já (??)
        Seller s = (Seller) this.getAgent();
        Product p = this.getProduct();
        
        // Other sellers are currenttly selling <product>
        // set selling price accordingly
        s.addProduct(p, this.seller.getPricePickingStrategy().calculateInitialPrice(s, p));
        System.out.printf("! %s set product %s price at %f%n", this.getAgent().getLocalName(), this.getProduct().getName(), s.getProductPrice(this.getProduct().getName()));
        s.register(p);
    }
    // TODO: ver se vale a pena handlers da 1st part
}
