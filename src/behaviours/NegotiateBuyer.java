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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
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
                // TODO: ver
                System.out.printf("// TODO: There was no product for buyer %s searching for %s%n",
                        this.getAgent().getLocalName(), this.product);
                this.buyer.noSellerForProduct(this.product);
                if (this.buyer.finished()) {
                    System.out.println("Buyer bought all stuffs");
                    this.buyer.doDelete();
                }
                return v;
            }

            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                if(!this.getAgent().isScammer(result[i].getName()))
                    cfp.addReceiver(result[i].getName());

            // TODO: igual a cima ambos têm de ter condição de saída no onEnd
            if(!cfp.getAllReceiver().hasNext()){
                System.out.printf("// TODO: There are no good sellers left for buyer %s searching for %s%n",
                        this.getAgent().getLocalName(), this.product);
                this.buyer.noSellerForProduct(this.product);
                if (this.buyer.finished()) {
                    System.out.printf("No sellers for product %n", this.product);
                    this.buyer.doDelete();
                }
                return v;
            }

        
            // A "blank" offer (with -1) is sent to know the price of the product (we don't
            // send only the <product> because of compability reasons)
            cfp.setContentObject(new OfferInfo(this.product, -1));

        } catch (FIPAException | IOException fe) {
            // TODO Auto-generated catch block
            fe.printStackTrace();
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
                sb.append(String.format("%n - %s sent a REFUSE.", msg.getSender().getLocalName()));
            } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    SellerOfferInfo sellerOffer = (SellerOfferInfo) msg.getContentObject();
                    offers.put(msg.getSender(), sellerOffer);
                    sb.append(String.format("%n - %s with seller offer %s.", msg.getSender().getLocalName(),
                            sellerOffer));
                } catch (UnreadableException e) {
                    sb.append(String.format("%n - %s containing invalid content.", msg.getSender().getLocalName()));
                }
            } else {
                sb.append(String.format("%n - %s sent a %s.", msg.getSender().getLocalName(), ACLMessage.getPerformative(msg.getPerformative())));
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
        Map<AID, SellerOfferInfo> offers = this.getOffers(convertedResponses);

        // Update with new SellerOffers and new counter-offers
        // If counterOffers is empty it means that the lastOffer contains the lowest
        // prices possible
        Map<AID, OfferInfo> counterOffers = this.buyer.getCounterOfferStrategy().pickOffers(offers,
                this.previousOffers, this.ownPreviousOffer);

        // TODO: we should also add something for "sooner is better than waiting"
        // (because the products can be bought by others while we wait)

        if (counterOffers.isEmpty()) {
            this.prepareFinalMessages(convertedResponses, acceptances);
        } else {
            this.prepareCounterOfferMessages(counterOffers, convertedResponses, acceptances);
            newIteration(acceptances);
        }
    }

    private void updateWaitingList(ACLMessage incomingMessage, List<ACLMessage> outgoingMessages, StringBuilder sb) {

        AID bestSeller = this.buyer.getCounterOfferStrategy().finalDecision(this.previousOffers);
        String format = "%n - %s";
        // If the best negotiation that is on wait is no longer a candidate, reject it
        if (this.negotiationOnWait != null && bestSeller != this.negotiationOnWait.getSender()) {
            outgoingMessages.add(this.prepareRejectProposal(this.negotiationOnWait));
            sb.append(String.format(format, negotiationOnWait.getSender().getLocalName()));
            this.negotiationOnWait = null;
        }

        // If msg, i.e. the ended negotiation, isn't the best among the rest cancel it
        if (bestSeller != incomingMessage.getSender()) {
            outgoingMessages.add(this.prepareRejectProposal(incomingMessage));
            sb.append(String.format(format, incomingMessage.getSender().getLocalName()));
        }
        // msg is the current best alternative, store it
        else
            this.negotiationOnWait = incomingMessage;
    }

    private void prepareCounterOfferMessages(Map<AID, OfferInfo> counterOffers, List<ACLMessage> incomingMessages, List<ACLMessage> outgoingMessages) {
        // Do the counterOffers while the others "wait"
        StringBuilder sbCFP = new StringBuilder(String.format("< %s sent CFP on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s sent REJECT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        boolean reject = false;

        // For each incoming message, check if a counter offer has been made
        for (ACLMessage msg : incomingMessages) {
            ACLMessage rep = msg.createReply();

            // If negotiation is to continue send the chosen counter offer
            if (counterOffers.containsKey(msg.getSender())) {
                rep.setPerformative(ACLMessage.CFP);

                this.ownPreviousOffer.put(msg.getSender(), counterOffers.get(msg.getSender()));
                try {
                    rep.setContentObject(counterOffers.get(msg.getSender()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sbCFP.append(String.format("%n - %s : %s", msg.getSender().getLocalName(),
                        counterOffers.get(msg.getSender())));
                outgoingMessages.add(rep);
            }
            // Negotiation has halted, only store if it would be the best option right now.
            else {
                reject = true;
                this.updateWaitingList(msg, outgoingMessages, sbReject);
            }
        }
        if (reject)
            sbCFP.append(sbReject.toString());
        if (this.negotiationOnWait != null)
            sbCFP.append(String.format("%n! %s is now on wait.%n", this.negotiationOnWait.getSender()));
        this.getAgent().logger().info(sbCFP.toString());
    }

    private void prepareFinalMessages(List<ACLMessage> lastMessages, List<ACLMessage> outgoingMessages) {
        StringBuilder sbAccept = new StringBuilder(String.format("< %s sent ACCEPT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));
        StringBuilder sbReject = new StringBuilder(String.format("%n< %s sent REJECT_PROPOSAL on round %d:", this.getAgent().getLocalName(), this.negotiationRound));        

        // Choose the best seller among the possibilities
        AID bestSeller = this.buyer.getCounterOfferStrategy().finalDecision(this.previousOffers);

        // Get all messages that need answering: all from this round + the message on
        // wait if any
        List<ACLMessage> pendingMessages = new ArrayList<>();

        pendingMessages.addAll(lastMessages);

        if (this.negotiationOnWait != null)
            pendingMessages.add(this.negotiationOnWait);

        boolean reject = false;
        for (ACLMessage msg : pendingMessages) {
            ACLMessage rep = msg.createReply();

            if(msg.getPerformative() != ACLMessage.PROPOSE) {
                continue;
            }

            // Accept the proposal of the best offer and reject all others
            if (msg.getSender().equals(bestSeller)) {
                rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                try {
                    SellerOfferInfo bestOffer = (SellerOfferInfo) msg.getContentObject();
                    rep.setContentObject(new OfferInfo(bestOffer.getProduct(), bestOffer.getOfferedPrice()));
                    sbAccept.append(String.format("%n - %s : %s", msg.getSender().getLocalName(), bestOffer));
                } catch (UnreadableException | IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                reject = true;
                rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                sbReject.append(String.format("%n - %s", msg.getSender().getLocalName()));
            }
            outgoingMessages.add(rep);
        }
        if(reject)
            sbAccept.append(sbReject.toString());
        this.getAgent().logger().info(sbAccept.toString());
    }

    @Override
    protected void handleInform(ACLMessage inform) {

        Object response;
        try {
            response = inform.getContentObject();
            this.getAgent().logger().info(String.format("< %s received INFORM from agent %s with %s", this.getAgent().getLocalName(), inform.getSender().getLocalName(), response));

            if (response instanceof Scam) {
                Scam scam = (Scam) response;
                this.buyer.changeMoneySpent(scam.getOfferInfo().getOfferedPrice());
                this.getAgent().logger().info(String.format("< %s was SCAMMED by agent %s with %s", this.getAgent().getLocalName(), inform.getSender().getLocalName(), response));
                this.getAgent().addScammer(inform.getSender());
            } else if (response instanceof OfferInfo) {
                OfferInfo offerInfo = (OfferInfo) response;
                this.buyer.receivedProduct(offerInfo.getProduct());
                this.buyer.changeMoneySpent(offerInfo.getOfferedPrice());
                Stats.updateMoneySaved(this.buyer);
                if (this.buyer.finished()) {
                    this.buyer.doDelete();
                }
            }

        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        // TODO: Manel
        this.getAgent().logger().info(String.format("> %s received FAILURE from agent %s with %s",
                this.getAgent().getLocalName(), failure.getSender().getLocalName(), failure.getContent()));
    }

    // Helpers

    private ACLMessage prepareRejectProposal(ACLMessage msg) {
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
        System.out.println("On End: " + this.getAgent().getLocalName());
        if (this.getAgent().isBuying(this.product)) {
            this.reinitiate();
            this.getAgent().getBehaviour().addSubBehaviour(this);
        }
        return super.onEnd();
    }
}
