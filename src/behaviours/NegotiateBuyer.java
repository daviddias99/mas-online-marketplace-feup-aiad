package behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import agents.Buyer;
import agents.counterOfferStrategies.CounterOfferStrategy;
import models.OfferInfo;
import models.Product;
import models.SellerOfferInfo;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class NegotiateBuyer extends ContractNetInitiator {
    private Product product;
    private CounterOfferStrategy counterOfferStrategy;
    private int negotiationRound;
    private Map<AID, SellerOfferInfo> previousOffers;
    private ACLMessage negotiationOnWait;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp, CounterOfferStrategy counterOfferStrategy) {
        super(b, cfp);
        this.product = product;
        this.counterOfferStrategy = counterOfferStrategy;
        this.negotiationRound = 0;
        this.previousOffers = new HashMap<>();
        this.negotiationOnWait = null;
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {

        Vector<ACLMessage> v = new Vector<ACLMessage>();

        // Query df service for agents who are selling <product>
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);

            // No agents are selling <product>
            if (result.length == 0) {
                // TODO: ver
                System.out.printf("// TODO: There was no product for buyer %s searching for %s%n",
                        this.getAgent().getLocalName(), this.product);
                return v;
            }

            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                cfp.addReceiver(result[i].getName());

        } catch (FIPAException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
        }

        // A "blank" offer (with -1) is sent to know the price of the product (we don't
        // send only the <product> because of compability reasons)
        try {
            cfp.setContentObject(new OfferInfo(this.product, -1));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return v;
        }

        v.add(cfp);

        return v;
    }

    private Map<AID, SellerOfferInfo> getOffers(Vector<ACLMessage> receivedMessages) {
        Map<AID, SellerOfferInfo> offers = new HashMap<>();
        // Filter the valid offers
        for (ACLMessage msg : receivedMessages) {
            if (msg.getPerformative() != ACLMessage.PROPOSE) {
                // TODO: confirmar se comment está certo. não é verdade. depois vai poder vir
                // refuses
                // Should never get here
                System.out.printf("> Message from %s indicating a failure.%n", msg.getSender().getLocalName());
                continue;
            }
            try {
                SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                offers.put(msg.getSender(), sellerOffer);
            } catch (UnreadableException e) {
                System.out.printf("> Message from %s contained invalid content.", msg.getSender().getLocalName());
            }
        }

        return offers;
    }


    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        this.negotiationRound++;
        System.out.printf("> %s got %d responses on round %d!%n", this.getAgent().getLocalName(), responses.size(),
                this.negotiationRound);

        // Seller product offers
        Vector<ACLMessage> convertedResponses = responses;
        Map<AID, SellerOfferInfo> offers = this.getOffers(convertedResponses);

        // Update with new SellerOffers and new counter-offers
        // If counterOffers is empty it means that the lastOffer contains the lowest
        // prices possible
        Map<AID, OfferInfo> counterOffers = this.counterOfferStrategy.pickOffers(offers, this.previousOffers);

        // TODO: we should also add something for "sooner is better than waiting"
        // (because the products can be bought by others while we wait)

        if (counterOffers.isEmpty()) {
            this.prepareFinalMessages(convertedResponses, responses);
        } else {
            this.prepareCounterOfferMessages(counterOffers, convertedResponses, responses);
            newIteration(acceptances);
        }
    }

    private void updateWaitingList(ACLMessage msg, Vector<ACLMessage> outgoingMessages) {

        AID bestSeller = this.counterOfferStrategy.finalDecision(this.previousOffers);

        // If the best negotiation that is on wait is no longer a candidate, reject it
        if (bestSeller != this.negotiationOnWait.getSender()) {
            ACLMessage response = this.negotiationOnWait.createReply();
            response.setPerformative(ACLMessage.REJECT_PROPOSAL);
            outgoingMessages.add(response);
            this.negotiationOnWait = null;
        }

        // If msg, i.e. the ended negotiation, isn't the best among the rest cancel it
        if (bestSeller != msg.getSender()) {
            ACLMessage response = msg.createReply();
            response.setPerformative(ACLMessage.REJECT_PROPOSAL);
            outgoingMessages.add(response);
        }
        // msg is the current best alternative, store it
        else {
            this.negotiationOnWait = msg;
        }

    }

    private void prepareCounterOfferMessages(Map<AID, OfferInfo> counterOffers, Vector<ACLMessage> incomingMessages,
            Vector outgoingMessages) {
        // Do the counterOffers while the others "wait"
        for (ACLMessage msg : incomingMessages) {
            ACLMessage rep = msg.createReply();

            // If negotiation is to continue send the chosen counter offer
            if (counterOffers.containsKey(msg.getSender())) {
                rep.setPerformative(ACLMessage.CFP);
                try {
                    rep.setContentObject(counterOffers.get(msg.getSender()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                outgoingMessages.add(rep);
            }
            // Negotiation has halted, only store if it would be the best option right now.
            else {
                this.updateWaitingList(msg, outgoingMessages);
            }
        }
    }

    private void prepareFinalMessages(Vector<ACLMessage> incomingMessages, Vector outgoingMessages) {
        // TODO: se calhar verificar se está vazio
        AID bestSeller = this.counterOfferStrategy.finalDecision(this.previousOffers);

        // Get all messages that need answering: all from this round + the message on wait if any
        Vector<ACLMessage> pendingMessages = new Vector<ACLMessage>();
        
        if(this.negotiationOnWait != null)
            pendingMessages.add(this.negotiationOnWait);

        for (ACLMessage msg : pendingMessages) {
            ACLMessage rep = msg.createReply();

            // Accept the proposal of the best offer and reject all others
            if (msg.getSender().equals(bestSeller)) {
                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                SellerOfferInfo bestOffer = this.previousOffers.get(bestSeller);
                try {
                    rep.setContentObject(new OfferInfo(bestOffer.getProduct(), bestOffer.getOfferedPrice()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }
            outgoingMessages.add(rep);
        }
    }


    protected void handleAllResultNotifications(Vector resultNotifications) {
        // TODO: complete
        System.out.println("got " + resultNotifications.size() + " result notifs!");
    }
}
