package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import agents.Buyer;
import models.OfferInfo;
import models.Product;
import models.Scam;
import models.SellerOfferInfo;
import jade.core.AID;
import sajas.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import sajas.proto.ContractNetInitiator;
import utils.Stats;

public class NegotiateBuyer extends ContractNetInitiator {
    private Product product;
    private int negotiationRound;
    private Map<AID, SellerOfferInfo> previousOffers;
    private Map<AID, OfferInfo> ownPreviousOffer;
    private ACLMessage negotiationOnWait;
    private Buyer buyer;

    public NegotiateBuyer(Product product, Buyer b, ACLMessage cfp) {
        super(b, cfp);
        this.product = product;
        this.negotiationRound = 0;
        this.previousOffers = new ConcurrentHashMap<>();
        this.ownPreviousOffer = new ConcurrentHashMap<>();
        this.negotiationOnWait = null;
        this.buyer = b;
    }

    @Override
    public Buyer getAgent() {
        return (Buyer) super.getAgent();
    }

    void leaveDueToNoAvailableSellers(boolean dueToLackOfHonesty) {
        this.buyer.logger().info(String.format("! %s did not find any %s seller for %s", this.buyer.getLocalName(),
                dueToLackOfHonesty ? "honest" : "", this.product));
        this.buyer.noSellerForProduct(this.product);
        if (this.buyer.finished()) {
            System.out.printf("! %s is leaving because there are no available sellers %n", this.buyer.getLocalName());
            this.buyer.logger().info(String.format("! %s is leaving %n", this.buyer));
            this.buyer.doDelete();
        }

    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {

        Vector<ACLMessage> v = new Vector<>();

        // Query df service for agents who are selling <product>
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);

            // No agents are selling <product>
            if (result.length == 0) {
                this.leaveDueToNoAvailableSellers(false);
                return v;
            }


            String log = String.format("< %s ask price of %s to sellers:", this.getAgent().getLocalName(), this.product.getName());
            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                if (!this.getAgent().isScammer( result[i].getName())) {
                    cfp.addReceiver(result[i].getName());
                    log += String.format("%n - %s", result[i].getName().getLocalName());
                }

            this.buyer.logger()
                    .info(log);

            // No valid receivers found
            if (!cfp.getAllReceiver().hasNext()) {
                this.leaveDueToNoAvailableSellers(true);
                return v;
            }


            // A "blank" offer (with -1) is sent to know the price of the product (we don't
            // send only the <product> because of compability reasons)
            cfp.setContentObject(new OfferInfo(this.product, -1));

        } catch (IOException | FIPAException fe) {
            this.buyer.logger()
                    .warning(String.format("/!\\ %s could not send initial offer for product %s%n", this.buyer.getLocalName(), this.product));
            return v;
        }

        v.add(cfp);
        return v;
    }

    private Map<AID, SellerOfferInfo> getOffers(List<ACLMessage> receivedMessages) {
        Map<AID, SellerOfferInfo> offers = new HashMap<>();
        StringBuilder sb = new StringBuilder(String.format("> %s got %d responses on round %d:",
                this.buyer.getLocalName(), receivedMessages.size(), this.negotiationRound));

        // Filter the valid offers
        for (ACLMessage msg : receivedMessages) {

            if (msg.getPerformative() == ACLMessage.FAILURE) {
                this.handleFailure(msg);
            } else if (msg.getPerformative() == ACLMessage.INFORM) {
                this.handleInform(msg);
            } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                sb.append(String.format("%n - %s sent a REFUSE (Seller no longer selling %s).", msg.getSender().getLocalName(), this.product.getName()));
                this.previousOffers.remove(msg.getSender());
            } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                    offers.put( msg.getSender(), sellerOffer);
                    sb.append(String.format("%n - %s with seller offer %s.", msg.getSender().getLocalName(),
                            sellerOffer));
                } catch (UnreadableException e) {
                    sb.append(String.format("%n - %s containing invalid content.", msg.getSender().getLocalName()));
                }
            } else {
                sb.append(String.format("%n - %s sent a %s.", msg.getSender().getLocalName(),
                        ACLMessage.getPerformative(msg.getPerformative())));
            }

        }
        this.buyer.logger().info(sb.toString());

        return offers;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        this.negotiationRound++;

        // Seller product offers
        List<ACLMessage> convertedResponses = responses;
        List<ACLMessage> convertedAcceptances = acceptances;
        Map<AID, SellerOfferInfo> offers = this.getOffers(convertedResponses);

        // Update with new SellerOffers and new counter-offers
        // If counterOffers is empty it means that the lastOffer contains the lowest
        // prices possible
        Map<AID, OfferInfo> counterOffers = this.buyer.getCounterOfferStrategy().pickOffers(offers, this.previousOffers,
                this.ownPreviousOffer, this.negotiationRound);

        // (because the products can be bought by others while we wait)
        if (counterOffers.isEmpty()) {
            this.prepareFinalMessages(convertedResponses, convertedAcceptances);
        } else if(!offers.isEmpty()){
            this.prepareCounterOfferMessages(counterOffers, convertedResponses, convertedAcceptances);
            newIteration(acceptances);
        }
    }

    private boolean updateWaitingList(ACLMessage incomingMessage, List<ACLMessage> outgoingMessages, StringBuilder sb) {

        boolean rejected = false;
        StringBuilder decisionSB = new StringBuilder(String.format("! %s reached agreement with %s for %s:", this.getAgent().getLocalName(), incomingMessage.getSender().getLocalName(),this.product.getName()));
        AID bestSeller = this.buyer.getCounterOfferStrategy().makeDecision(this.previousOffers, this.buyer, decisionSB);
        decisionSB.append(String.format("%n conclusion: best seller is %s", bestSeller.getLocalName()));
        this.buyer.logger().info(decisionSB.toString());
        String format = "%n - %s";
        // If the best negotiation that is on wait is no longer a candidate, reject it
        if (this.negotiationOnWait != null && !bestSeller.equals(this.negotiationOnWait.getSender())) {
            outgoingMessages.add(this.prepareRejectProposal(this.negotiationOnWait));
            sb.append(String.format(format, negotiationOnWait.getSender().getLocalName()));
            this.negotiationOnWait = null;
            rejected = true;
        }

        // If msg, i.e. the ended negotiation, isn't the best among the rest cancel it
        if (!bestSeller.equals(incomingMessage.getSender())) {
            outgoingMessages.add(this.prepareRejectProposal(incomingMessage));
            sb.append(String.format(format, incomingMessage.getSender().getLocalName()));
            rejected = true;
        }
        // msg is the current best alternative, store it
        else
            this.negotiationOnWait = incomingMessage;

        return rejected;
    }

    private void prepareCounterOfferMessages(Map<AID, OfferInfo> counterOffers, List<ACLMessage> incomingMessages,
            List<ACLMessage> outgoingMessages) {
        // Do the counterOffers while the others "wait"
        StringBuilder sbCFP = new StringBuilder(
                String.format("< %s sent CFP on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s sent REJECT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.negotiationRound));
        boolean reject = false;
        ACLMessage previouslyOnWait = this.negotiationOnWait;
        // For each incoming message, check if a counter offer has been made
        for (ACLMessage msg : incomingMessages) {
            ACLMessage rep = msg.createReply();

            // If negotiation is to continue send the chosen counter offer
            if (counterOffers.containsKey(msg.getSender())) {
                rep.setPerformative(ACLMessage.CFP);

                try {
                    rep.setContentObject(counterOffers.get(msg.getSender()));
                } catch (IOException e) {
                    this.buyer.logger()
                    .warning(String.format("/!\\ %s could not send counter offer object %n", this.buyer.getLocalName()));
                    continue;
                }
                sbCFP.append(String.format("%n - %s : %s", msg.getSender().getLocalName(),
                        counterOffers.get(msg.getSender())));
                outgoingMessages.add(rep);
            }
            // Negotiation has halted, only store if it would be the best option right now.
            else {
                reject |= this.updateWaitingList(msg, outgoingMessages, sbReject);
            }
        }

        // Logging
        if (reject)
            sbCFP.append(sbReject.toString());
        if (this.negotiationOnWait != null && this.negotiationOnWait != previouslyOnWait)
            sbCFP.append(String.format("%n! %s is now on wait.", this.negotiationOnWait.getSender().getLocalName()));
        this.getAgent().logger().info(sbCFP.toString());
    }

    private void prepareFinalMessages(List<ACLMessage> lastMessages, List<ACLMessage> outgoingMessages) {

        StringBuilder message = new StringBuilder();
        StringBuilder sbAccept = new StringBuilder(String.format("< %s sent ACCEPT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s sent REJECT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.negotiationRound));

        // Choose the best seller among the possibilities
        StringBuilder decisionSB = new StringBuilder(String.format("! %s final decision for %s:", this.getAgent().getLocalName(), this.product.getName()));
        AID bestSeller = this.buyer.getCounterOfferStrategy().makeDecision(this.previousOffers, this.buyer, decisionSB);
        this.buyer.logger().info(decisionSB.toString());

        // Get all messages that need answering: all from this round + the message on
        // wait if any
        List<ACLMessage> pendingMessages = new ArrayList<>();

        pendingMessages.addAll(lastMessages);

        if (this.negotiationOnWait != null)
            pendingMessages.add(this.negotiationOnWait);

        boolean reject = false;
        boolean accept = false;
        for (ACLMessage msg : pendingMessages) {
            ACLMessage rep = msg.createReply();

            if (msg.getPerformative() != ACLMessage.PROPOSE) {
                continue;
            }

            // Accept the proposal of the best offer and reject all others
            if (msg.getSender().equals(bestSeller)) {
                accept = true;
                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                try {
                    SellerOfferInfo bestOffer = (SellerOfferInfo) msg.getContentObject();
                    rep.setContentObject(new OfferInfo(bestOffer.getProduct(), bestOffer.getOfferedPrice()));
                    sbAccept.append(String.format("%n - %s : %s", msg.getSender().getLocalName(), bestOffer));
                } catch (UnreadableException | IOException e1) {
                    this.buyer.logger()
                    .warning(String.format("/!\\ %s could not send accept proposal %n", this.buyer.getLocalName()));
                    continue;
                }
            } else {
                reject = true;
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                sbReject.append(String.format("%n - %s", msg.getSender().getLocalName()));
            }
            outgoingMessages.add(rep);
        }

        // Logging
        if (accept)
            message.append(sbAccept.toString());

        if (reject)
            message.append(sbReject.toString());

        if(message.length() != 0)
            this.getAgent().logger().info(message.toString());
    }

    private void handleScam(Scam scam, ACLMessage inform){

        this.buyer.changeMoneySpent(scam.getOfferInfo().getOfferedPrice());
        Stats.updateMoneySaved(this.buyer);
        this.getAgent().logger().info(String.format("! %s was SCAMMED by agent %s with %s",
                this.getAgent().getLocalName(), inform.getSender().getLocalName(), scam));
        
        this.getAgent().addScammer( inform.getSender());
    }

    private void handleSuccessfulAcquisition(OfferInfo offerInfo, ACLMessage inform){

        this.getAgent().logger().info(String.format("! %s BOUGHT %s from agent %s",
        this.getAgent().getLocalName(), offerInfo, inform.getSender().getLocalName()));
        this.buyer.receivedProduct(offerInfo.getProduct());
        this.buyer.changeMoneySpent(offerInfo.getOfferedPrice());
        Stats.updateMoneySaved(this.buyer);
        if (this.buyer.finished()) {
            this.buyer.logger().info(String.format("! %s bought every product he needed", this.buyer.getLocalName()));
            System.out.printf("! %s bought every product he needed%n", this.buyer.getLocalName());
            this.buyer.doDelete();
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {

        Object response;
        try {
            response = inform.getContentObject();
            this.getAgent().logger().info(String.format("< %s received INFORM from agent %s with %s",
                    this.getAgent().getLocalName(), inform.getSender().getLocalName(), response));

            if (response instanceof Scam) {
                this.handleScam((Scam)response, inform);
            } else if (response instanceof OfferInfo) {
                this.handleSuccessfulAcquisition((OfferInfo)response, inform);
            }

        } catch (UnreadableException e) {
            this.getAgent().logger().info(String.format("/!\\ %s could not read object sent by %s",
                    this.getAgent().getLocalName(), inform.getSender().getLocalName()));
        }

    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        this.getAgent().logger().info(String.format("> %s received FAILURE from agent %s with %s",
                this.getAgent().getLocalName(), failure.getSender().getLocalName(), failure.getContent()));
    }

    // Helpers

    private ACLMessage prepareRejectProposal(ACLMessage msg) {
        this.previousOffers.remove(msg.getSender());
        this.ownPreviousOffer.remove(msg.getSender());
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
        return response;
    }

    private void reinitiate() {
        this.negotiationRound = 0;
        this.previousOffers = new ConcurrentHashMap<>();
        this.negotiationOnWait = null;
        reset(new ACLMessage(ACLMessage.CFP));
    }

    @Override
    public int onEnd() {
        if (this.getAgent().isBuying(this.product)) {
            this.reinitiate();
            this.getAgent().addBehaviour(this);
        }
        return super.onEnd();
    }
}
