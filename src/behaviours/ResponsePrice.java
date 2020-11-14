package behaviours;

import agents.Seller;
import models.Product;
import models.SellerOfferInfo;

import java.io.IOException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class ResponsePrice extends AchieveREResponder {

    public ResponsePrice(Seller a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    public Seller getAgent(){
        return (Seller) super.getAgent();
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) {
        ACLMessage reply = request.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        return reply;
    }
    
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage result = request.createReply();

        // Inform querying agent of the offered price and own credibility
        try {
            result.setPerformative(ACLMessage.INFORM);

            Product productRequested = (Product)request.getContentObject();
            Seller s = this.getAgent();
            Product respProduct = s.getProduct(productRequested.getName());

            SellerOfferInfo info = new SellerOfferInfo(respProduct,s.getProductPrice(respProduct.getName()),s.getCredibility());
            s.logger().info(String.format("< %s sent to %s price %s", s.getLocalName(), request.getSender().getLocalName(), info));
            result.setContentObject(info);

        } catch ( UnreadableException | IOException e) {
            result.setPerformative(ACLMessage.FAILURE);
            result.setContent(e.getMessage());
        }

        return result;
    }
}
