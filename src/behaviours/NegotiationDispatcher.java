package behaviours;

import agents.Seller;
import agents.offerStrategies.OfferStrategy;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSResponderDispatcher;

public class NegotiationDispatcher extends SSResponderDispatcher {

    private OfferStrategy offerStrategy;

    public NegotiationDispatcher(Agent a, MessageTemplate tpl, OfferStrategy offerStrategy) {
        super(a, tpl);
        this.offerStrategy = offerStrategy;
    }

    @Override
    protected Behaviour createResponder(ACLMessage arg0) {
        return new NegotiateSeller((Seller)this.getAgent(), arg0, this.offerStrategy);
    }    
}
