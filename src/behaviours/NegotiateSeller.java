package behaviours;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import agents.Buyer;
import agents.Seller;
import models.OfferInfo;
import models.Product;
import models.Scam;
import models.SellerOfferInfo;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;

public class NegotiateSeller extends SSIteratedContractNetResponder {

    private Map<Product, ConcurrentHashMap<AID, OfferInfo>> previousOffers;

    public NegotiateSeller(Seller s, ACLMessage cfp) {
        super(s, cfp);

        this.previousOffers = new ConcurrentHashMap<>();
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();

        Seller seller = this.getAgent();
        try {
            OfferInfo buyerOffer = (OfferInfo) cfp.getContentObject();
            seller.logger().info(String.format("> %s received CFP from agent %s with %s", seller.getLocalName(),
                    cfp.getSender().getLocalName(), buyerOffer));

            // If it still has the product
            if (seller.hasProduct(buyerOffer.getProduct())) {

                // Create the products record if it doesn't exist
                this.previousOffers.putIfAbsent(buyerOffer.getProduct(), new ConcurrentHashMap<>());

                // Calculate price to propose
                OfferInfo previousOffer = this.previousOffers.get(buyerOffer.getProduct()).get(cfp.getSender());
                float originalPrice = seller.getProductPrice(buyerOffer.getProduct().getName());
                float offeredPrice = seller.getOfferStrategy().chooseOffer(buyerOffer, previousOffer, originalPrice);

                SellerOfferInfo sellerOffer = new SellerOfferInfo(buyerOffer.getProduct(), offeredPrice,
                        seller.getCredibility());

                // Update previous offer record
                this.previousOffers.get(buyerOffer.getProduct()).put(cfp.getSender(), buyerOffer);

                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(sellerOffer);
                seller.logger().info(String.format("< %s sending PROPOSE to agent %s with %s", seller.getLocalName(),
                        cfp.getSender().getLocalName(), sellerOffer));
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                seller.logger().info(String.format("< %s sending REFUSE to agent %s", seller.getLocalName(),
                        cfp.getSender().getLocalName()));
            }

        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
            seller.logger().warning(String.format("< %s sending REFUSE to agent %s with error %s",
                    seller.getLocalName(), cfp.getSender().getLocalName(), e.getMessage()));
        }

        return reply;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO: later
        this.getAgent().logger().info(String.format("> %s received REJECT from agent %s",
                this.getAgent().getLocalName(), reject.getSender().getLocalName()));
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {

        OfferInfo buyerOffer;
        Seller seller = this.getAgent();
        ACLMessage result = accept.createReply();

        try {
            buyerOffer = (OfferInfo) accept.getContentObject();
            seller.logger().info(String.format("> %s received ACCEPT from agent %s with offer %s",
                    seller.getLocalName(), accept.getSender().getLocalName(), buyerOffer));
            String content;
            OfferInfo maxProposal = this.bestCurrentOfferFor(buyerOffer.getProduct());

            boolean scam = this.getAgent().doScam();

            // Cancel the sale, better offer still at play.
            if (maxProposal.getOfferedPrice() > buyerOffer.getOfferedPrice()) {
                result.setPerformative(ACLMessage.FAILURE);
                content = "Sorry, a better deal came up...";
                result.setContent(content);

                seller.logger().info(String.format("< %s sent %s to agent %s saying %s, credibility", seller.getLocalName(),
                ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(), content));
            }
            // Has product and will scam the buyer
            else if (scam && seller.hasProduct(buyerOffer.getProduct())) {
                Scam scamObj = new Scam(buyerOffer);
                result.setPerformative(ACLMessage.INFORM);
                result.setContentObject(scamObj);
                seller.changeWealth(maxProposal.getOfferedPrice());
                int oldCredibility = seller.getCredibility();
                int newCredibility = seller.reduceCredibility();
                seller.logger()
                        .info(String.format("< %s sent %s to agent %s saying %s, credibility %d -> %d",
                                seller.getLocalName(), ACLMessage.getPerformative(result.getPerformative()),
                                cfp.getSender().getLocalName(), scamObj, oldCredibility, newCredibility));                   
            }
            // The product will be sold.
            else if (seller.removeProduct(buyerOffer.getProduct()) != null) {

                result.setPerformative(ACLMessage.INFORM);
                result.setContentObject(buyerOffer);
                content = buyerOffer.toString();

                // Increase wealth
                seller.changeWealth(buyerOffer.getOfferedPrice());

                // Increase credibility
                int oldCredibility = seller.getCredibility();
                int newCredibility = seller.increaseCredibility();
                
                seller.deregister(buyerOffer.getProduct());

                seller.logger().info(String.format("< %s sent %s to agent %s saying %s, credibility %d -> %d", seller.getLocalName(),
                ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(), content, oldCredibility, newCredibility));
            }
            // Cancel the sale, already was sold.
            else {
                result.setPerformative(ACLMessage.FAILURE);
                content = "Sorry, a better deal came up, already sold it...";
                result.setContent(content);

                seller.logger().info(String.format("< %s sent %s to agent %s saying %s", seller.getLocalName(),
                ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(), content));
            }

            // Clear offer history from agent
            this.previousOffers.get(buyerOffer.getProduct()).remove(cfp.getSender());

        } catch (UnreadableException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
