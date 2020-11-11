package behaviours;

import java.io.IOException;

import agents.Seller;
import models.OfferInfo;
import models.SellerOfferInfo;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;

public class NegotiateSeller extends SSIteratedContractNetResponder {

    public NegotiateSeller(Seller s, ACLMessage cfp) {
        super(s, cfp);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();

        try {
            OfferInfo productRequested = (OfferInfo) cfp.getContentObject();
            Seller s = (Seller) this.getAgent();
            
            // If it still has the product
            if(s.hasProduct(productRequested.getProduct())){
                reply.setPerformative(ACLMessage.PROPOSE);
                // TODO: fazer estratégia de contrapropostas. para já manda sempre preço original
                SellerOfferInfo info = new SellerOfferInfo(productRequested.getProduct(), s.getProductPrice(productRequested.getProduct().getName()), s.getCredibility());
                reply.setContentObject(info);
                System.out.printf(" > SEND: %s with %s to %s%n", this.getAgent().getLocalName(), info, cfp.getSender().getLocalName());

            }
            else{
                reply.setPerformative(ACLMessage.REFUSE);
                System.out.printf(" > SEND: %s with REFUSE to %s%n", this.getAgent().getLocalName(), cfp.getSender().getLocalName());
            }

        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
        }
        
        return reply;
    }
    
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO: later
        System.out.println(myAgent.getLocalName() + " got a reject...");
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        // TODO: later
        // TODO: Se ainda tiver o produto aceite enviar INFORM qq cena
        // Else - se já não tiver (foi comprado por outro entretanto), enviar FAILURE
        System.out.println(myAgent.getLocalName() + " got an accept!");
        ACLMessage result = accept.createReply();
        result.setPerformative(ACLMessage.INFORM);
        result.setContent("We are done");
        
        return result;
    }

    
}
