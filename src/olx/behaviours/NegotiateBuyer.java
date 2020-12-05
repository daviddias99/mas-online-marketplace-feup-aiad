package olx.behaviours;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import olx.Olx;
import olx.agents.Buyer;
import olx.draw.Edge;
import olx.models.OfferInfo;
import olx.models.Product;
import olx.models.Scam;
import olx.models.SellerOfferInfo;
import jade.core.AID;
import sajas.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import sajas.proto.ContractNetInitiator;
import uchicago.src.sim.network.DefaultDrawableNode;
import olx.utils.Stats;
import olx.utils.Util;

public class NegotiateBuyer extends ContractNetInitiator {
    private static final long serialVersionUID = 1L;

    private Product product;
    private int index;
    private int negotiationRound;
    private Map<AID, SellerOfferInfo> previousOffers;
    private Map<AID, OfferInfo> ownPreviousOffer;
    private ACLMessage negotiationOnWait;
    private String conversationID;

    public NegotiateBuyer(Product product, int index, Buyer b, ACLMessage cfp) {
        super(b, cfp);
        this.product = product;
        this.index = index;
        this.negotiationRound = 0;
        this.previousOffers = new ConcurrentHashMap<>();
        this.ownPreviousOffer = new ConcurrentHashMap<>();
        this.negotiationOnWait = null;
        this.conversationID = "_" + this.product.getName() + "_" + this.index;
    }

    @Override
    public Buyer getAgent() {
        return (Buyer) super.getAgent();
    }

    void leaveDueToNoAvailableSellers(boolean dueToLackOfHonesty) {
        Buyer b = this.getAgent();
        b.logger().info(String.format("! %s did not find any %s seller for %s", b.getLocalName(),
                dueToLackOfHonesty ? "honest" : "", this.product));
        b.noSellerForProduct(this.product);
        if (b.finished()) {
            System.out.printf("! %s is leaving because there are no available sellers %n", b.getLocalName());
            b.logger().info(String.format("! %s is leaving %n", b));
            b.doDelete();
        }
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
        cfp.setConversationId(this.getAgent().getLocalName() + this.conversationID);

        Vector<ACLMessage> v = new Vector<>();

        // Query df service for olx.agents who are selling <product>
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);

            // No olx.agents are selling <product>
            if (result.length == 0) {
                this.leaveDueToNoAvailableSellers(false);
                return v;
            }


            String log = String.format("< %s(%s) ask price of %s to sellers:", this.getAgent().getLocalName(), this.conversationID, this.product.getName());
            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                if (!this.getAgent().isScammer( result[i].getName())) {
                    cfp.addReceiver(result[i].getName());
                    log += String.format(Util.LIST_FORMAT, result[i].getName().getLocalName());
                }

            this.getAgent().logger().info(log);

            // No valid receivers found
            if (!cfp.getAllReceiver().hasNext()) {
                this.leaveDueToNoAvailableSellers(true);
                return v;
            }


            // A "blank" offer (with -1) is sent to know the price of the product (we don't
            // send only the <product> because of compability reasons)
            cfp.setContentObject(new OfferInfo(this.product, -1));

        } catch (IOException | FIPAException fe) {
            this.getAgent().logger().warning(String.format("/!\\ %s(%s) could not send initial offer for product %s%n", this.getAgent().getLocalName(), this.conversationID, this.product));
            return v;
        }
        v.add(cfp);
        return v;
    }

    private Map<AID, SellerOfferInfo> getOffers(List<ACLMessage> receivedMessages) {
        Map<AID, SellerOfferInfo> offers = new HashMap<>();
        StringBuilder sb = new StringBuilder(String.format("> %s(%s) got %d responses on round %d:",
                this.getAgent().getLocalName(), this.conversationID, receivedMessages.size(), this.negotiationRound));

        // Filter the valid offers
        for (ACLMessage msg : receivedMessages) {

            switch (msg.getPerformative()) {
                case ACLMessage.FAILURE:
                    this.handleFailure(msg);
                    break;
                case ACLMessage.INFORM:
                    this.handleInform(msg);
                    break;
                case ACLMessage.REFUSE:
                    sb.append(String.format(Util.LIST_FORMAT + " sent a REFUSE (Seller no longer selling %s).", msg.getSender().getLocalName(), this.product.getName()));
                    this.previousOffers.remove(msg.getSender());
                    break;
                case ACLMessage.PROPOSE:
                    try {
                        SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                        offers.put(msg.getSender(), sellerOffer);
                        sb.append(String.format(Util.LIST_FORMAT + " with seller offer %s.", msg.getSender().getLocalName(),
                                sellerOffer));
                    } catch (UnreadableException e) {
                        sb.append(String.format(Util.LIST_FORMAT + " containing invalid content.", msg.getSender().getLocalName()));
                    }
                    break;
                default:
                    sb.append(String.format(Util.LIST_FORMAT + " sent a %s.", msg.getSender().getLocalName(), ACLMessage.getPerformative(msg.getPerformative())));
                    break;
            }
        }
        this.getAgent().logger().info(sb.toString());

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
        Map<AID, OfferInfo> counterOffers = this.getAgent().getCounterOfferStrategy().pickOffers(offers, this.previousOffers,
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
        StringBuilder decisionSB = new StringBuilder(String.format("! %s(%s) reached agreement with %s for %s:", this.getAgent().getLocalName(), this.conversationID, incomingMessage.getSender().getLocalName(),this.product.getName()));
        AID bestSeller = this.getAgent().getCounterOfferStrategy().makeDecision(this.previousOffers, this.getAgent(), decisionSB);
        decisionSB.append(String.format("%n conclusion: best seller is %s", bestSeller.getLocalName()));
        this.getAgent().logger().info(decisionSB.toString());
        // If the best negotiation that is on wait is no longer a candidate, reject it
        // If we find out the waiting negotiation is from a scammer, also reject it
        if (this.negotiationOnWait != null && (!bestSeller.equals(this.negotiationOnWait.getSender()) ||  this.getAgent().isScammer(negotiationOnWait.getSender()) )) {
            outgoingMessages.add(this.prepareRejectProposal(this.negotiationOnWait));
            sb.append(String.format(Util.LIST_FORMAT, negotiationOnWait.getSender().getLocalName()));
            this.negotiationOnWait = null;
            rejected = true;
        }

        // If msg, i.e. the ended negotiation, isn't the best among the rest cancel it
        // Negotiations from scammers are rejected
        if (!bestSeller.equals(incomingMessage.getSender()) || this.getAgent().isScammer(incomingMessage.getSender())) {
            outgoingMessages.add(this.prepareRejectProposal(incomingMessage));
            sb.append(String.format(Util.LIST_FORMAT, incomingMessage.getSender().getLocalName()));
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
                String.format("< %s(%s) sent CFP on round %d:", this.getAgent().getLocalName(), this.conversationID, this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s(%s) sent REJECT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.conversationID, this.negotiationRound));
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
                    this.getAgent().logger().warning(String.format("/!\\ %s(%s) could not send counter offer object %n", this.getAgent().getLocalName(), this.conversationID));
                    continue;
                }
                sbCFP.append(String.format(Util.LIST_FORMAT + " : %s", msg.getSender().getLocalName(),
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
        StringBuilder sbAccept = new StringBuilder(String.format("< %s(%s) sent ACCEPT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.conversationID, this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s(%s) sent REJECT_PROPOSAL on round %d:",
                this.getAgent().getLocalName(), this.conversationID, this.negotiationRound));

        // Choose the best seller among the possibilities
        StringBuilder decisionSB = new StringBuilder(String.format("! %s(%s) final decision for %s:", this.getAgent().getLocalName(), this.conversationID, this.product.getName()));
        AID bestSeller = this.getAgent().getCounterOfferStrategy().makeDecision(this.previousOffers, this.getAgent(), decisionSB);
        this.getAgent().logger().info(decisionSB.toString());

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
            if (msg.getSender().equals(bestSeller) && !this.getAgent().isScammer(msg.getSender())) {
                accept = true;
                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                try {
                    SellerOfferInfo bestOffer = (SellerOfferInfo) msg.getContentObject();
                    rep.setContentObject(new OfferInfo(bestOffer.getProduct(), bestOffer.getOfferedPrice()));
                    sbAccept.append(String.format(Util.LIST_FORMAT + " : %s", msg.getSender().getLocalName(), bestOffer));
                } catch (UnreadableException | IOException e1) {
                    this.getAgent().logger().warning(String.format("/!\\ %s(%s) could not send accept proposal %n", this.getAgent().getLocalName(), this.conversationID));
                    continue;
                }
            } else {
                reject = true;
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                sbReject.append(String.format(Util.LIST_FORMAT, msg.getSender().getLocalName()));
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
        DefaultDrawableNode myNode = this.getAgent().getNode();
        if (myNode != null) {
            DefaultDrawableNode to = Olx.getNode(Util.localNameToLabel(inform.getSender().getLocalName()));
            Edge edge = new Edge(myNode, to);
            edge.setColor(Color.ORANGE);
            myNode.addOutEdge(edge);
        }

        this.getAgent().changeMoneySpent(scam.getOfferInfo().getOfferedPrice());
        Stats.updateMoneySaved(this.getAgent());
        this.getAgent().logger().info(String.format("! %s(%s) was SCAMMED by agent %s with %s",
                this.getAgent().getLocalName(), this.conversationID, inform.getSender().getLocalName(), scam));
        this.getAgent().addScammer(inform.getSender());
    }

    private void handleSuccessfulAcquisition(OfferInfo offerInfo, ACLMessage inform){
        DefaultDrawableNode myNode = this.getAgent().getNode();
        if (myNode != null) {
            DefaultDrawableNode to = Olx.getNode(Util.localNameToLabel(inform.getSender().getLocalName()));
            Edge edge = new Edge(myNode, to);
            edge.setColor(Color.GREEN);
            myNode.addOutEdge(edge);
        }

        Buyer b = this.getAgent();
        b.logger().info(String.format("! %s BOUGHT %s from agent %s",
        b.getLocalName(), offerInfo, inform.getSender().getLocalName()));
        b.receivedProduct(offerInfo.getProduct(), this.index);
        b.changeMoneySpent(offerInfo.getOfferedPrice());
        Stats.updateMoneySaved(b);
        if (b.finished()) {
            b.logger().info(String.format("! %s bought every product he needed", b.getLocalName()));
            System.out.printf("! %s bought every product he needed%n", b.getLocalName());
            b.doDelete();
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {

        Object response;
        try {
            response = inform.getContentObject();
            this.getAgent().logger().info(String.format("< %s(%s) received INFORM from agent %s with %s",
                    this.getAgent().getLocalName(), this.conversationID, inform.getSender().getLocalName(), response));

            if (response instanceof Scam) {
                this.handleScam((Scam)response, inform);
            } else if (response instanceof OfferInfo) {
                this.handleSuccessfulAcquisition((OfferInfo)response, inform);
            }

        } catch (UnreadableException e) {
            this.getAgent().logger().info(String.format("/!\\ %s(%s) could not read object sent by %s",
                    this.getAgent().getLocalName(), this.conversationID, inform.getSender().getLocalName()));
        }

    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        this.getAgent().logger().info(String.format("> %s(%s) received FAILURE from agent %s with %s",
                this.getAgent().getLocalName(), this.conversationID, failure.getSender().getLocalName(), failure.getContent()));
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
        if (this.getAgent().isBuying(this.product, this.index)) {
            this.reinitiate();
            this.getAgent().addBehaviour(this);
        }
        return super.onEnd();
    }
}
