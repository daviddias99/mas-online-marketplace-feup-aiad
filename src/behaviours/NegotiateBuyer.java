package behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import agents.Buyer;
import agents.strategies.SellerPickingStrategy;
import models.Product;
import models.SellerOfferInfo;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class NegotiateBuyer extends ContractNetInitiator {
    private Product product;
    private SellerPickingStrategy sellerPickingStrategy;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp, SellerPickingStrategy sellerPickingStrategy) {
        super(b, cfp);
        this.product = product;
        this.sellerPickingStrategy = sellerPickingStrategy;
    }

    protected Vector prepareCfps(ACLMessage cfp) {

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
                // TODO: ver
                System.out.printf("// TODO: There was no product for buyer %s searching for %s%n", this.getAgent().getLocalName(), this.product);
                return v;
            }

            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                cfp.addReceiver(result[i].getName());

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        // The <product> is sent as the content so that the 
        // seller knows to which product the request pertains to
        try {
            cfp.setContentObject(this.product);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return v;
        }

        v.add(cfp);

        return v;
    }
    
    protected void handleAllResponses(Vector responses, Vector acceptances) {
			
        System.out.printf("> %s got %d responses!%n", this.getAgent().getLocalName(), responses.size());
        
        // Seller product offers
        Map<AID,SellerOfferInfo> offers = new HashMap<>();

        Vector<ACLMessage> convertedResponses = responses;

        for(ACLMessage msg : convertedResponses){
            if(msg.getPerformative() != ACLMessage.PROPOSE){
                // TODO: confirmar se comment está certo
                // Should never get here
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

        // Chose seller with which to negotiate
        // TODO: depois por mais que um talvez (ciclo à frente já conta com isso)
        AID chosenSeller = this.sellerPickingStrategy.pickSeller(offers);
        System.out.printf("> Picked seller %s%n", chosenSeller.getLocalName());

        for(ACLMessage msg : convertedResponses){
            ACLMessage rep = msg.createReply();
            // TODO: depois mudar quando o pickSeller escolher vários
            if(chosenSeller.equals(msg.getSender()))
                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            else
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);

            acceptances.add(rep);
        }

    }
    
    protected void handleAllResultNotifications(Vector resultNotifications) {
        // TODO: complete
        System.out.println("got " + resultNotifications.size() + " result notifs!");
    }
}
