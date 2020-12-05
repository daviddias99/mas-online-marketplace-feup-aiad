package behaviours;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import agents.Seller;
import models.OfferInfo;
import models.Product;
import models.Scam;
import models.SellerOfferInfo;
import models.Stock;
import utils.Util;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;
import utils.Stats;

public class NegotiateSeller extends SSIteratedContractNetResponder {
    private static final long serialVersionUID = 1L;
    
    private Map<Product, ConcurrentHashMap<AID, OfferInfo>> previousOffers;
    private Map<Product, ConcurrentHashMap<AID, SellerOfferInfo>> ownPreviousOffers;
    private List<AID> sentOffers;

    public NegotiateSeller(Seller s, ACLMessage cfp) {
        super(s, cfp);

        this.previousOffers = new ConcurrentHashMap<>();
        this.ownPreviousOffers = new ConcurrentHashMap<>();
        this.sentOffers = new LinkedList<>();
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();

        Seller seller = this.getAgent();
        try {
            OfferInfo buyerOffer = (OfferInfo) cfp.getContentObject();
            seller.logger().info(String.format("> %s (%s) received CFP from agent %s with %s", seller.getLocalName(), cfp.getConversationId(),
                    cfp.getSender().getLocalName(), buyerOffer));

            // If it still has the product
            if (seller.hasProduct(buyerOffer.getProduct())) {

                // Create the products record if it doesn't exist
                this.previousOffers.putIfAbsent(buyerOffer.getProduct(), new ConcurrentHashMap<>());
                this.ownPreviousOffers.putIfAbsent(buyerOffer.getProduct(), new ConcurrentHashMap<>());

                // Calculate price to propose
                OfferInfo previousOffer = this.previousOffers.get(buyerOffer.getProduct()).get(cfp.getSender());
                float offeredPrice = 0;

                if (previousOffer != null
                        && Util.floatEqual(buyerOffer.getOfferedPrice(), previousOffer.getOfferedPrice())) {
                    offeredPrice = previousOffer.getOfferedPrice();
                } else {
                    offeredPrice = seller.getOfferStrategy().chooseOffer(buyerOffer, previousOffer,
                            this.ownPreviousOffers.get(buyerOffer.getProduct()).get(cfp.getSender()), seller);
                }

                SellerOfferInfo sellerOffer = new SellerOfferInfo(buyerOffer.getProduct(), offeredPrice,
                        seller.getCredibility());

                // Update previous offer record
                this.previousOffers.get(buyerOffer.getProduct()).put(cfp.getSender(), buyerOffer);

                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(sellerOffer);
                this.ownPreviousOffers.get(buyerOffer.getProduct()).put(cfp.getSender(), sellerOffer);
                seller.logger().info(String.format("< %s (%s) sending PROPOSE to agent %s with %s", seller.getLocalName(), cfp.getConversationId(),
                        cfp.getSender().getLocalName(), sellerOffer));
                this.sentOffers.add(cfp.getSender());
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                seller.logger().info(String.format("< %s (%s) sending REFUSE to agent %s", seller.getLocalName(), cfp.getConversationId(),
                        cfp.getSender().getLocalName()));
            }

        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
            seller.logger().warning(String.format("< %s (%s) sending REFUSE to agent %s with error %s",
                    seller.getLocalName(), cfp.getConversationId(), cfp.getSender().getLocalName(), e.getMessage()));
        }

        return reply;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {

        this.sentOffers.remove(reject.getSender());
        this.getAgent().logger().info(String.format("> %s (%s) received REJECT from agent %s",
                this.getAgent().getLocalName(), reject.getConversationId(),reject.getSender().getLocalName()));
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {

        OfferInfo buyerOffer = null;
        Seller seller = this.getAgent();
        ACLMessage result = accept.createReply();

        this.sentOffers.remove(accept.getSender());

        try {
            buyerOffer = (OfferInfo) accept.getContentObject();
        } catch (UnreadableException e) {
            result.setPerformative(ACLMessage.FAILURE);
            result.setContent("Could not understand content");
            seller.logger().warning(String.format("/!\\ %s (%s) could not read content sent by %s", seller.getLocalName(), accept.getConversationId(),
                    accept.getSender().getLocalName()));
            seller.logger().warning(
                    String.format("< %s (%s) sent FAILURE to %s", seller.getLocalName(), accept.getConversationId(), accept.getSender().getLocalName()));
            return result;
        }

        try {

            seller.logger().info(String.format("> %s (%s) received ACCEPT from agent %s with offer %s",
                    seller.getLocalName(), accept.getConversationId(), accept.getSender().getLocalName(), buyerOffer));
            String content;
            OfferInfo maxProposal = this.bestCurrentOfferFor(buyerOffer.getProduct());

            boolean scam = this.getAgent().doScam();
            Stock stock = null;

            // Cancel the sale, better offer still at play.
            if (maxProposal.getOfferedPrice() > buyerOffer.getOfferedPrice()) {
                result.setPerformative(ACLMessage.FAILURE);
                content = "Sorry, a better deal came up...";
                result.setContent(content);

                seller.logger()
                        .info(String.format("< %s (%s) sent %s to agent %s saying %s, credibility", seller.getLocalName(), cfp.getConversationId(),
                                ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(),
                                content));
            }
            // Has product and will scam the buyer
            else if (scam && seller.hasProduct(buyerOffer.getProduct())) {
                Scam scamObj = new Scam(buyerOffer);
                result.setPerformative(ACLMessage.INFORM);
                result.setContentObject(scamObj);
                seller.changeWealth(maxProposal.getOfferedPrice());
                int oldCredibility = seller.getCredibility();
                int newCredibility = seller.reduceCredibility();

                Stats.scam(seller, maxProposal.getOfferedPrice());
                seller.logger()
                        .info(String.format("< %s (%s) sent %s to agent %s saying %s, credibility %d -> %d",
                                seller.getLocalName(), cfp.getConversationId(), ACLMessage.getPerformative(result.getPerformative()),
                                cfp.getSender().getLocalName(), scamObj, oldCredibility, newCredibility));
            }
            // The product will be sold.
            else if ((stock = seller.removeProduct(buyerOffer.getProduct())) != null) {

                result.setPerformative(ACLMessage.INFORM);
                result.setContentObject(buyerOffer);
                content = buyerOffer.toString();

                // Increase wealth
                seller.changeWealth(buyerOffer.getOfferedPrice());

                // Increase credibility
                int oldCredibility = seller.getCredibility();
                int newCredibility = seller.increaseCredibility();

                // Deregister if no quantity left
                if(stock.empty())
                    seller.deregister(buyerOffer.getProduct());

                Stats.productSold(seller, buyerOffer.getProduct(), buyerOffer.getOfferedPrice());

                seller.logger().info(String.format("< %s (%s) sent %s to agent %s saying %s, credibility %d -> %d, quantity left=%d",
                                seller.getLocalName(), cfp.getConversationId() ,ACLMessage.getPerformative(result.getPerformative()),
                                cfp.getSender().getLocalName(), content, oldCredibility, newCredibility, stock.getQuantity()));   
            }
            // Cancel the sale, already was sold.
            else {
                result.setPerformative(ACLMessage.FAILURE);
                content = "Sorry, a better deal came up, already sold it...";
                result.setContent(content);

                seller.logger().info(String.format("< %s (%s) sent %s to agent %s saying %s", seller.getLocalName(), cfp.getConversationId(),
                        ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(), content));
            }

            // Clear offer history from agent
            this.previousOffers.get(buyerOffer.getProduct()).remove(cfp.getSender());

        } catch (IOException e) {
            result.setPerformative(ACLMessage.FAILURE);
            result.setContent("Could not send proper content");
            seller.logger().warning(String.format("/!\\ %s (%s) could not send content to %s", seller.getLocalName(), accept.getConversationId(),
                    accept.getSender().getLocalName()));
            seller.logger().warning(
                    String.format("< %s (%s) sent FAILURE to %s", seller.getLocalName(), accept.getConversationId(), accept.getSender().getLocalName()));
            return result;
        }

        return result;
    }

    // HELPER

    private OfferInfo bestCurrentOfferFor(Product product) {

        Map<AID, OfferInfo> offers = this.previousOffers.get(product);
        Entry<AID, OfferInfo> maxEntry = Collections.max(offers.entrySet(),
                (e1, e2) -> e1.getValue().compareTo(e2.getValue()));

        return maxEntry.getValue();
    }

    @Override
    public Seller getAgent() {
        return (Seller) super.getAgent();
    }
}
