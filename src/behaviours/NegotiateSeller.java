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

        try {
            OfferInfo buyerOffer = (OfferInfo) cfp.getContentObject();
            Seller seller = (Seller) this.getAgent();
            System.out.printf("> %s received CFP from agent %s saying %s%n", this.getAgent().getLocalName(), cfp.getSender().getLocalName(), buyerOffer);
            
            // If it still has the product
            if (seller.hasProduct(buyerOffer.getProduct())) {

                // Create the products record if it doesn't exist
                this.previousOffers.putIfAbsent(buyerOffer.getProduct(), new ConcurrentHashMap<>());

                // Calculate price to propose
                OfferInfo previousOffer = this.previousOffers.get(buyerOffer.getProduct()).get(cfp.getSender());
                float originalPrice = seller.getProductPrice(buyerOffer.getProduct().getName());
                float offeredPrice = this.offerStrategy.chooseOffer(buyerOffer, previousOffer, originalPrice);
                
                SellerOfferInfo sellerOffer = new SellerOfferInfo(buyerOffer.getProduct(), offeredPrice,
                seller.getCredibility());
                
                // Update previous offer record
                this.previousOffers.get(buyerOffer.getProduct()).put(cfp.getSender(), buyerOffer);

                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(sellerOffer);
                System.out.printf("< %s sending PROPOSE to agent %s saying: %s%n", this.getAgent().getLocalName(), cfp.getSender().getLocalName(), sellerOffer);
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                System.out.printf("< %s sending REFUSE to agent %s%n", this.getAgent().getLocalName(), cfp.getSender().getLocalName());
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
        System.out.printf("> %s received REJECT from agent %s%n", this.getAgent().getLocalName(), reject.getSender().getLocalName());
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {

        System.out.printf("> %s received ACCEPT from agent %s%n", this.getAgent().getLocalName(), accept.getSender().getLocalName());

        OfferInfo buyerOffer;
        Seller seller = (Seller) this.getAgent();
        ACLMessage result = accept.createReply();
        try {
            buyerOffer = (OfferInfo) cfp.getContentObject();

            if (seller.removeProduct(buyerOffer.getProduct()) != null) {
                
                OfferInfo maxProposal = this.bestCurrentOfferFor(buyerOffer.getProduct());

                 // Cancel the sale, better offer still at play.
                if(maxProposal.getOfferedPrice() > buyerOffer.getOfferedPrice()){
                    result.setPerformative(ACLMessage.FAILURE);
                    result.setContent("Sorry, a better deal came up...");
                }
                // The product will be sold.
                else{
                    result.setPerformative(ACLMessage.INFORM);
                    result.setContentObject(buyerOffer);
                    seller.changeWealth(maxProposal.getOfferedPrice());
                }
            } 
            // Cancel the sale, already was sold.
            else {
                result.setPerformative(ACLMessage.FAILURE);
                result.setContent("Sorry, a better deal came up, already sold it...");
            }

            System.out.printf("< %s sending %s to agent %s saying: %s%n", this.getAgent().getLocalName(), ACLMessage.getPerformative(result.getPerformative()), cfp.getSender().getLocalName(), result.getContent());

            // Clear offer history from agent
            this.previousOffers.get(buyerOffer.getProduct()).remove(cfp.getSender());

        } catch (UnreadableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return result;
    }

}
