package behaviours;

import agents.Seller;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.proto.SSResponderDispatcher;

public class NegotiationDispatcher extends SSResponderDispatcher {


    public NegotiationDispatcher(Agent a, MessageTemplate tpl) {
        super(a, tpl);
    }

    @Override
    protected Behaviour createResponder(ACLMessage arg0) {
        Seller seller = (Seller) this.getAgent();
        return new NegotiateSeller(seller, arg0);
    }    
}
