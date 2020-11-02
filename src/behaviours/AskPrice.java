package behaviours;

import models.Product;

import java.io.IOException;
import java.util.Vector;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public abstract class AskPrice extends AchieveREInitiator {

    private Product product;

    public AskPrice(Product product, Agent agent, ACLMessage msg) {
        super(agent, msg);
        this.product = product;
    }

    protected Product getProduct(){
        return this.product;
    }
    
    @Override
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
                this.handleNoResults();
                return v;
            }

            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        v.add(msg);

        return v;
    }

    protected abstract void handleNoResults();

    // TODO: ver se vale a pena handlers da 1st part
}
