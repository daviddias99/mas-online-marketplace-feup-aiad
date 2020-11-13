package behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import agents.Buyer;
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
    private int negotiationRound;
    private Map<AID, SellerOfferInfo> previousOffers;
    private ACLMessage negotiationOnWait;
    private Buyer buyer;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp) {
        super(b, cfp);
        this.product = product;
        this.negotiationRound = 0;
        this.previousOffers = new ConcurrentHashMap<>();
        this.negotiationOnWait = null;
        this.buyer = b;
    }

    @Override
    public Buyer getAgent(){
        return (Buyer) super.getAgent();
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
        Buyer s = this.getAgent();
        StringBuilder sb = new StringBuilder(String.format("> %s got %d responses on round %d:", s.getLocalName(), receivedMessages.size(), this.negotiationRound));

        // Filter the valid offers
        for (ACLMessage msg : receivedMessages) {
            if (msg.getPerformative() != ACLMessage.PROPOSE) {
                // TODO: confirmar se comment está certo. não é verdade. depois vai poder vir refuses
                // Should never get here
                sb.append(String.format("%n - %s indicating a failure.", msg.getSender().getLocalName()));
                continue;
            }
            try {
                SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                offers.put(msg.getSender(), sellerOffer);
                sb.append(String.format("%n - %s with seller offer %s.", msg.getSender().getLocalName(), sellerOffer));
            } catch (UnreadableException e) {
                sb.append(String.format("%n - %s containing invalid content.", msg.getSender().getLocalName()));
            }
        }
        s.logger.info(sb.toString());

        return offers;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        this.negotiationRound++;

        // Seller product offers
        Vector<ACLMessage> convertedResponses = responses;
        Map<AID, SellerOfferInfo> offers = this.getOffers(convertedResponses);

        // Update with new SellerOffers and new counter-offers
        // If counterOffers is empty it means that the lastOffer contains the lowest
        // prices possible
        Map<AID, OfferInfo> counterOffers = this.buyer.getCounterOfferStrategy().pickOffers(offers,
                this.previousOffers);

        // TODO: we should also add something for "sooner is better than waiting"
        // (because the products can be bought by others while we wait)

        if (counterOffers.isEmpty()) {
            this.prepareFinalMessages(convertedResponses, acceptances);
        } else {
            this.prepareCounterOfferMessages(counterOffers, convertedResponses, acceptances);
            newIteration(acceptances);
        }
    }

    private void updateWaitingList(ACLMessage msg, Vector<ACLMessage> outgoingMessages, StringBuilder sb) {

        AID bestSeller = this.buyer.getCounterOfferStrategy().finalDecision(this.previousOffers);
        String format = "%n - %s";
        // If the best negotiation that is on wait is no longer a candidate, reject it
        if (this.negotiationOnWait != null && bestSeller != this.negotiationOnWait.getSender()) {
            ACLMessage response = this.negotiationOnWait.createReply();
            response.setPerformative(ACLMessage.REJECT_PROPOSAL);
            outgoingMessages.add(response);
            sb.append(String.format(format, negotiationOnWait.getSender()));
            this.negotiationOnWait = null;
        }

        // If msg, i.e. the ended negotiation, isn't the best among the rest cancel it
        if (bestSeller != msg.getSender()) {
            ACLMessage response = msg.createReply();
            response.setPerformative(ACLMessage.REJECT_PROPOSAL);
            outgoingMessages.add(response);
            sb.append(String.format(format, negotiationOnWait.getSender()));
        }
        // msg is the current best alternative, store it
        else 
            this.negotiationOnWait = msg;
    }

    private void prepareCounterOfferMessages(Map<AID, OfferInfo> counterOffers, Vector<ACLMessage> incomingMessages, Vector outgoingMessages) {
        // Do the counterOffers while the others "wait"
        StringBuilder sbCFP = new StringBuilder(String.format("< %s sent CFP on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n> %s sent REJECT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        boolean reject = false;
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
                sbCFP.append(String.format("%n - %s : %s", msg.getSender().getLocalName(), counterOffers.get(msg.getSender())));
                outgoingMessages.add(rep);
            }
            // Negotiation has halted, only store if it would be the best option right now.
            else {
                if(!reject)
                    reject = true;
                this.updateWaitingList(msg, outgoingMessages, sbReject);
            }
        }
        if(reject)
            sbCFP.append(sbReject.toString());
        if(this.negotiationOnWait != null)
            sbCFP.append(String.format("%n! %s is now on wait.%n", this.negotiationOnWait.getSender()));
        this.getAgent().logger.info(sbCFP.toString());
    }

    private void prepareFinalMessages(Vector<ACLMessage> lastMessages, Vector outgoingMessages) {
        StringBuilder sbAccept = new StringBuilder(String.format("< %s sent ACCEPT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n> %s sent REJECT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));        

        // TODO: se calhar verificar se está vazio
        AID bestSeller = this.buyer.getCounterOfferStrategy().finalDecision(this.previousOffers);

        // Get all messages that need answering: all from this round + the message on
        // wait if any
        Vector<ACLMessage> pendingMessages = new Vector<ACLMessage>();
        boolean reject = false;


        pendingMessages.addAll(lastMessages);

        if (this.negotiationOnWait != null)
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

                sbAccept.append(String.format("%n - %s : %s", msg.getSender(), bestOffer));
            } else {
                if(!reject)
                    reject = true;
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                sbReject.append(String.format("%n - %s", msg.getSender()));
            }
            outgoingMessages.add(rep);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {

        OfferInfo info;
        try {
            info = (OfferInfo) inform.getContentObject();
            this.getAgent().logger.info(String.format("> %s received INFORM from agent %s with %s", this.getAgent().getLocalName(), inform.getSender().getLocalName(), info));
            this.buyer.changeWealth(-info.getOfferedPrice());
        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        // TODO: Manel
        this.getAgent().logger.info(String.format("> %s received FAILURE from agent %s with %s", this.getAgent().getLocalName(), failure.getSender().getLocalName(), failure.getContent()));
    }

}
