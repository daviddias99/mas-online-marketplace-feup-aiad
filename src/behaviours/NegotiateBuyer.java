package behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import agents.Buyer;
import agents.counterOfferStrategies.CounterOfferStrategy;
import agents.filteringStrategies.*;
import models.Negotiation;
import models.NegotiationRound;
import models.OfferInfo;
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
    private SellerFilteringStrategy sellerFilteringStrategy;
    private CounterOfferStrategy counterOfferStrategy;
    private int negotiationRound;
    private Map<AID,Negotiation> negotiationHistory;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp, SellerFilteringStrategy sellerFilteringStrategy, CounterOfferStrategy counterOfferStrategy) {
        super(b, cfp);
        this.product = product;
        this.sellerFilteringStrategy = sellerFilteringStrategy;
        this.counterOfferStrategy = counterOfferStrategy;
        this.negotiationRound = 0;
        this.negotiationHistory = new HashMap<>();
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {

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
            for (int i = 0; i < result.length; ++i){
                cfp.addReceiver(result[i].getName());

                // Create a new negotiation
                this.negotiationHistory.put(result[i].getName(),new Negotiation());
            }

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        // The <product> is sent as the content so that the seller knows to which product the request pertains to
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
    
    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        
        this.negotiationRound++;
        System.out.printf("> %s got %d responses!%n", this.getAgent().getLocalName(), responses.size());
        
        // Seller product offers
        Map<AID,SellerOfferInfo> offers = new HashMap<>();
        Vector<ACLMessage> convertedResponses = responses;

        // Filter the valid offers
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
        Map<AID, SellerOfferInfo> chosenSellers = this.sellerFilteringStrategy.pickSeller(offers,this.negotiationHistory);
        Map<AID, OfferInfo> counterOffers = this.counterOfferStrategy.pickOffers(offers,this.negotiationHistory);

        for (AID agent : negotiationHistory.keySet()) {
            if(chosenSellers.containsKey(agent)){
                negotiationHistory.get(agent).addRound(new NegotiationRound(this.negotiationRound, offers.get(agent).getOfferedPrice()));
                negotiationHistory.get(agent).finish();
            }
            else {
                negotiationHistory.get(agent).addRound(new NegotiationRound(this.negotiationRound, offers.get(agent).getOfferedPrice(),counterOffers.get(agent).getOfferedPrice()));
            }
        }

        // Send responses
        for(ACLMessage msg : convertedResponses){
            ACLMessage rep = msg.createReply();
           
            // If negotiation is to continue send the chosen counter offer
            if(chosenSellers.containsKey(msg.getSender())){

                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                try {
                    rep.setContentObject(counterOffers.get(msg.getSender()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // Else reject proposal
            else{

                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }

            acceptances.add(rep);
        }

    }

    private void logRoundSummary(){
        System.out.printf("--- ROUND %d SUMMARY (Product: %s) ---%n", this.negotiationRound, this.product.getName());
        
        for (AID agent : this.negotiationHistory.keySet()) {
            
        }
    }
    
    protected void handleAllResultNotifications(Vector resultNotifications) {
        // TODO: complete
        System.out.println("got " + resultNotifications.size() + " result notifs!");
    }
}
