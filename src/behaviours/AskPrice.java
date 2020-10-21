package src.behaviours;

import src.agents.Buyer;

import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

// TODO: ver se Behaviour ou SimpleBehaviour
public class AskPrice extends Behaviour {
    private Buyer buyer;
    private boolean done = false;


    public AskPrice(Buyer buyer){
        this.buyer = buyer;
    }

    @Override
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("seller");
        template.addServices(sd);

        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.setContent(this.buyer.getProduct());
        
        try {
            DFAgentDescription[] result = DFService.search(this.buyer, template);
            for(int i=0; i<result.length; ++i) {
                msg.addReceiver(result[i].getName());
                this.buyer.send(msg);
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        this.done = true;
    }

    @Override
    public boolean done() {
        return this.done;
    }
}
