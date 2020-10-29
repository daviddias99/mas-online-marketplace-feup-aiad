package behaviours;

import agents.Buyer;
import models.Product;

import java.io.IOException;
import java.util.Vector;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class AskPrice extends AchieveREInitiator {

    private Product product;

    public AskPrice(Product product, Buyer buyerAgent, ACLMessage msg) {
        super(buyerAgent, msg);
        this.product = product;
    }

    protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
        Vector<ACLMessage> v = new Vector<ACLMessage>();

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            msg.setContentObject(this.product);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return v;
        }

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);
            if(result.length == 0){
                System.out.println("// TODO: No available sellers at this time");
                return v;
            }

            for (int i = 0; i < result.length; ++i) {
                msg.addReceiver(result[i].getName());
            }
        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        v.add(msg);

        return v;
    }

    // TODO: ver se vale a pena handlers da 1st part

    // TODO: escolher entre handleAllResultNotifications (analisar todos de uma vez
    // no final)
    // e como o professor tem
    protected void handleInform(ACLMessage inform) {
        try {
            Product productReponse = (Product)inform.getContentObject();
            System.out.printf(" < RECEIVED: %s with %s from %s\n", this.getAgent().getLocalName(), productReponse, inform.getSender().getLocalName());
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    protected void handleFailure(ACLMessage failure) {
        System.out.println(failure);
    }
}
