package src.behaviours;

import src.agents.Seller;

import java.io.IOException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
            result.setContentObject((Seller) this.getAgent());
        } catch (IOException e) {
            result.setPerformative(ACLMessage.FAILURE);
            result.setContent(e.getMessage());
        }

        return result;
    }
}
