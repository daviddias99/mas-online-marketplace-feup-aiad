package agents.strategies.counter_offer;

import java.util.Map;

import agents.Buyer;
import jade.core.AID;
import models.OfferInfo;
import models.SellerOfferInfo;
import utils.Util;

public class SmartCounterOfferStrategy extends CounterOfferStrategy {

    @Override
    public AID makeDecision(Map<AID, SellerOfferInfo> offers, Buyer buyer) {
        
        AID bestDecision = null;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<AID, SellerOfferInfo> entry : offers.entrySet()) {
            SellerOfferInfo offer = entry.getValue();
            
            // Lower is better
            // More credibility -> Lower cost
            // More rounds -> Higher cost

            float patienceDiscount = (float) Math.pow(buyer.getPatience()/100.0f, offer.getRound());
            float perceivedOfferCost = offer.getOfferedPrice() / offer.getSellerCredibility() /patienceDiscount ;
            buyer.logger().info(String.format("! %s evaluated %s from %s as %f",buyer.getLocalName(), offer, entry.getKey().getLocalName(), perceivedOfferCost));

            if(perceivedOfferCost < bestValue){
                bestValue = perceivedOfferCost;
                bestDecision = entry.getKey();
            }
        }

        return bestDecision;
    }

    @Override
    protected float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer) {
        
        if(ownPreviousOffer == null){
            return Util.round(0.5f * offer.getOfferedPrice(), 2);
        }
        else{
            float variance =  this.getVariance(offer.getProduct(),  (offer.getOfferedPrice() - ownPreviousOffer.getOfferedPrice())/4);
            return Math.min(ownPreviousOffer.getOfferedPrice() + variance, offer.getOfferedPrice());
        }
    }
    
}
