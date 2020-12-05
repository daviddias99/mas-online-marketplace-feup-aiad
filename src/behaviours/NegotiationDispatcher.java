package behaviours;

import agents.Seller;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSResponderDispatcher;

public class NegotiationDispatcher extends SSResponderDispatcher {
    private static final long serialVersionUID = 1L;

    public NegotiationDispatcher(Agent a, MessageTemplate tpl) {
        super(a, tpl);
    }

    @Override
    protected Behaviour createResponder(ACLMessage arg0) {
        Seller seller = (Seller) this.getAgent();
        return new NegotiateSeller(seller, arg0);
    }    
}
