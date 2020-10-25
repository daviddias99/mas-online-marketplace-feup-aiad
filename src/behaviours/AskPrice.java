package src.behaviours;

import src.agents.Buyer;

import java.util.Vector;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;


public class AskPrice extends AchieveREInitiator {

    public AskPrice(Buyer a, ACLMessage msg) {
        super(a, msg);
    }

    protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
        Vector<ACLMessage> v = new Vector<ACLMessage>();

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        // TODO: por depois certo o produto q se procura
        sd.setType("seller");
        template.addServices(sd);
        
        msg.setContent(((Buyer)this.getAgent()).getProduct());
        
        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);
            for(int i=0; i<result.length; ++i) {
                msg.addReceiver(result[i].getName());
                v.add(msg);
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }
        
        v.add(msg);

        return v;
    }

    // TODO: ver se vale a pena handlers da 1st part

    // TODO: escolher entre handleAllResultNotifications (analisar todos de uma vez no final)
    // e como o professor tem
    protected void handleInform(ACLMessage inform) {
        System.out.println(inform);
    }
    protected void handleFailure(ACLMessage failure) {
        System.out.println(failure);
    }
}
