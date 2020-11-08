package behaviours;

import java.io.IOException;

import agents.Seller;
import models.Product;
import models.SellerOfferInfo;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;

public class NegotiateSeller extends SSIteratedContractNetResponder {

    public NegotiateSeller(Seller s, ACLMessage cfp) {
        super(s, cfp);
    }

    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();

        try {
            reply.setPerformative(ACLMessage.PROPOSE);

            Product productRequested = (Product) cfp.getContentObject();
            Seller s = (Seller) this.getAgent();
            // TODO: confirmar depois q é mesmo preciso 2 products
            Product respProduct = s.getProduct(productRequested.getName());

            SellerOfferInfo info = new SellerOfferInfo(respProduct, s.getProductPrice(respProduct.getName()), s.getCredibility());
            System.out.printf(" > SEND: %s with %s to %s%n", this.getAgent().getLocalName(), info, cfp.getSender().getLocalName());
            reply.setContentObject(info);

        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
        }
        
        return reply;
    }
    
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO: later
        System.out.println(myAgent.getLocalName() + " got a reject...");
    }

    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        // TODO: later
        System.out.println(myAgent.getLocalName() + " got an accept!");
        ACLMessage result = accept.createReply();
        result.setPerformative(ACLMessage.INFORM);
        result.setContent("We are done");
        
        return result;
    }

    
}
