package src.behaviours;

import src.agents.Seller;
import src.models.Product;

import java.io.IOException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class ResponsePrice extends AchieveREResponder {
    
    public ResponsePrice(Seller a, MessageTemplate mt) {
        super(a, mt);
    }

    protected ACLMessage handleRequest(ACLMessage request) {
        ACLMessage reply = request.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        return reply;
    }
    
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage result = request.createReply();

        try {
            result.setPerformative(ACLMessage.INFORM);

            Product productRequested = (Product)request.getContentObject();

            Seller s = (Seller) this.getAgent();
            Product respProduct = s.getProduct(productRequested.getName());
            System.out.printf(" > SEND: %s with %s to %s\n", this.getAgent().getLocalName(), respProduct, request.getSender().getLocalName());
            result.setContentObject(respProduct);

        } catch ( UnreadableException | IOException e) {
            result.setPerformative(ACLMessage.FAILURE);
            result.setContent(e.getMessage());
        }

        return result;
    }
}
