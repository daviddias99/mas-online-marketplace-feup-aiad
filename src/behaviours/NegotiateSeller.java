package behaviours;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import agents.Seller;
import agents.offerStrategies.OfferStrategy;
import models.OfferInfo;
import models.Product;
import models.SellerOfferInfo;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;

public class NegotiateSeller extends SSIteratedContractNetResponder {

    private Map<Product, ConcurrentHashMap<AID, OfferInfo>> previousOffers;
    private OfferStrategy offerStrategy;

    public NegotiateSeller(Seller s, ACLMessage cfp, OfferStrategy offerStrategy) {
        super(s, cfp);
        this.previousOffers = new ConcurrentHashMap<>();
        this.offerStrategy = offerStrategy;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();

        System.out.printf("> %s received CFP from agent %s saying %s%n", this.getAgent().getLocalName(), cfp.getSender(), cfp.getContent());
        try {
            OfferInfo buyerOffer = (OfferInfo) cfp.getContentObject();
            Seller seller = (Seller) this.getAgent();

            // If it still has the product
            if (seller.hasProduct(buyerOffer.getProduct())) {

                reply.setPerformative(ACLMessage.PROPOSE);
                float offeredPrice = this.offerStrategy.chooseOffer(buyerOffer,
                        this.previousOffers.get(buyerOffer.getProduct()).get(cfp.getSender()));
                SellerOfferInfo sellerOffer = new SellerOfferInfo(buyerOffer.getProduct(), offeredPrice,
                        seller.getCredibility());
                reply.setContentObject(sellerOffer);
                System.out.printf("< %s sending PROPOSE to agent %s saying: %s%n", this.getAgent().getLocalName(), cfp.getSender(), sellerOffer);
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                System.out.printf("< %s sending REFUSE to agent %s%n", this.getAgent().getLocalName(), cfp.getSender());
            }

        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
        }

        return reply;
    }

    private OfferInfo bestCurrentOfferFor(Product product) {

        Map<AID, OfferInfo> offers = this.previousOffers.get(product);
        Entry<AID, OfferInfo> maxEntry = Collections.max(offers.entrySet(), (e1, e2) -> e1.getValue().compareTo(e2.getValue()));

        return maxEntry.getValue();
    }


    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO: later
        System.out.printf("> %s received REJECT from agent %s%n", this.getAgent().getLocalName(), reject.getSender());
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {

        System.out.printf("> %s received ACCEPT from agent %s%n", this.getAgent().getLocalName(), accept.getSender());

        OfferInfo buyerOffer;
        Seller seller = (Seller) this.getAgent();
        ACLMessage result = accept.createReply();
        try {
            buyerOffer = (OfferInfo) cfp.getContentObject();

            if (seller.hasProduct(buyerOffer.getProduct())) {

                OfferInfo maxProposal = this.bestCurrentOfferFor(buyerOffer.getProduct());

                 // Cancel the sale, better offer still at play.
                if(maxProposal.getOfferedPrice() > buyerOffer.getOfferedPrice()){
                    result.setPerformative(ACLMessage.FAILURE);
                    result.setContent("Sorry, a better deal came up, already sold it...");
                }
                // The product will be sold.
                else{
                    result.setPerformative(ACLMessage.INFORM);
                    result.setContent("We are done");
                }
            } 
            // Cancel the sale, already was sold.
            else {
                result.setPerformative(ACLMessage.FAILURE);
                result.setContent("Sorry, a better deal came up, already sold it...");
            }

            System.out.printf("< %s sending %s to agent %s saying: %s%n", this.getAgent().getLocalName(), result.getPerformative(), cfp.getSender(), result.getContent());

            // Clear offer history from agent
            this.previousOffers.get(buyerOffer.getProduct()).remove(cfp.getSender());

        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return result;
    }

}
