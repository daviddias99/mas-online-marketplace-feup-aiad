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
    private Map<AID,SellerOfferInfo> previousOffers;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp, SellerFilteringStrategy sellerFilteringStrategy, CounterOfferStrategy counterOfferStrategy) {
        super(b, cfp);
        this.product = product;
        this.sellerFilteringStrategy = sellerFilteringStrategy;
        this.counterOfferStrategy = counterOfferStrategy;
        this.negotiationRound = 0;
        this.previousOffers = new HashMap<>();
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
            for (int i = 0; i < result.length; ++i)
                cfp.addReceiver(result[i].getName());

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        // A "blank" offer (with -1) is sent to know the price of the product (we don't send only the <product> because of compability reasons)
        try {
            cfp.setContentObject(new OfferInfo(this.product, -1));
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
        System.out.printf("> %s got %d responses on round %d!%n", this.getAgent().getLocalName(), responses.size(), this.negotiationRound++);
        
        // Seller product offers
        Map<AID,SellerOfferInfo> offers = new HashMap<>();
        Vector<ACLMessage> convertedResponses = responses;

        // Filter the valid offers
        for(ACLMessage msg : convertedResponses){
            if(msg.getPerformative() != ACLMessage.PROPOSE){
                // TODO: confirmar se comment está certo. não é verdade. depois vai poder vir refuses
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

        // Update with new SellerOffers and new counterOffers
        Map<AID, OfferInfo> counterOffers = this.counterOfferStrategy.pickOffers(offers,this.previousOffers);
        // If counterOffers is empty it means that the lastOffer contains the lowest prices possible
        // TODO: we should also add something for "sooner is better than waiting" (because the products can be bought by others while we wait)
        if(counterOffers.isEmpty()){
            // TODO: se calhar verificar se não é null
            AID bestSeller = this.counterOfferStrategy.finalDesicion(this.previousOffers);
            
            // TODO: tenho quase a certeza q n basta enviar msgs para os response pq nós pusemos alguns à espera. temos de guardar replies prontas para aqui
            for(ACLMessage msg : convertedResponses) {
                ACLMessage rep = msg.createReply();

                if(msg.getSender().equals(bestSeller)){
                    rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    SellerOfferInfo bestOffer = this.previousOffers.get(bestSeller);
                    try {
                        rep.setContentObject(new OfferInfo(bestOffer.getProduct(), bestOffer.getOfferedPrice()));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else{
                    rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }

                acceptances.add(rep);
            }
        }
        else{
            // Do the counterOffers while the others "wait" and newIteration
            for(ACLMessage msg : convertedResponses) {
                ACLMessage rep = msg.createReply();

                // If negotiation is to continue send the chosen counter offer
                if(counterOffers.containsKey(msg.getSender())){
                    rep.setPerformative(ACLMessage.CFP);
                    try {
                        rep.setContentObject(counterOffers.get(msg.getSender()));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    acceptances.add(rep);
                }
                // Don't reject the others now. Keep them waiting
                // TODO: se não é mais baixo e se sei q não é para continuar, posso já mandar embora (REFUSE)
            }
            newIteration(acceptances);
        }
    }

    // private void logRoundSummary(){
        // System.out.printf("--- ROUND %d SUMMARY (Product: %s) ---%n", this.negotiationRound, this.product.getName());
        
        // for (AID agent : this.negotiationHistory.keySet()) {
            
        // }
    // }
    
    protected void handleAllResultNotifications(Vector resultNotifications) {
        // TODO: complete
        System.out.println("got " + resultNotifications.size() + " result notifs!");
    }
}
