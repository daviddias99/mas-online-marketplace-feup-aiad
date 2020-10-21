package src.behaviours;

import src.agents.Seller;

import java.io.IOException;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


public class BuyersListener extends CyclicBehaviour {
    private Seller seller;
    
    public BuyersListener(Seller seller){
        this.seller = seller;
    }

    @Override
    public void action() {
        ACLMessage msg = this.seller.receive();

        if (msg != null) {
            // TODO: pôr depois dependendo do tipo
            System.out.println(msg);
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            try {
                reply.setContentObject(this.seller);
                this.seller.send(reply);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            block();
        }

    }
}
