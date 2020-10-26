package src.behaviours;

import src.agents.Buyer;
import src.models.Product;

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

        for (Product p : ((Buyer) this.getAgent()).getMissingProducts()) {
            sd.setType(p.getName());
            template.addServices(sd);

            try {
                msg.setContentObject(p);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

            try {
                DFAgentDescription[] result = DFService.search(this.getAgent(), template);
                for (int i = 0; i < result.length; ++i) {
                    msg.addReceiver(result[i].getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            
            v.add(msg);
            // TODO: sera q se tem de fazer isto?
            template.removeServices(sd);
        }

        return v;
    }

    // TODO: ver se vale a pena handlers da 1st part

    // TODO: escolher entre handleAllResultNotifications (analisar todos de uma vez
    // no final)
    // e como o professor tem
    protected void handleInform(ACLMessage inform) {
        try {
            System.out.printf("%s received offer %s from %s\n",this.getAgent().getName(),(Product)inform.getContentObject(), inform.getSender().getName());
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    protected void handleFailure(ACLMessage failure) {
        System.out.println(failure);
    }
}
