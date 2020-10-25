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

    public AskPrice(Buyer a, ACLMessage msg) {
        super(a, msg);
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
                    v.add(msg);
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

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
            System.out.println((Product)inform.getContentObject());
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    protected void handleFailure(ACLMessage failure) {
        System.out.println(failure);
    }
}
