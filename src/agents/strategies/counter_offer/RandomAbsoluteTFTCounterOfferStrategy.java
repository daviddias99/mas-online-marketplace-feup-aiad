package agents.strategies.counter_offer;

import java.util.Map;

import agents.Buyer;
import jade.core.AID;
import models.OfferInfo;
import models.SellerOfferInfo;
import utils.Util;

public class RandomAbsoluteTFTCounterOfferStrategy extends CounterOfferStrategy {


    @Override
    protected float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer) {

        if(ownPreviousOffer == null){
            return Util.round(0.5f * offer.getOfferedPrice(), 2);
        }

        float previouslyOfferedPrice = ownPreviousOffer.getOfferedPrice();
        float currentSellerOffer = offer.getOfferedPrice();
        float baseIncrement = Math.max(offer.getProduct().getOriginalPrice() * 0.1f, 1.0f);
        float incrementValue = Util.randomFloatBetween(-baseIncrement / 2, baseIncrement / 2);
        float newOffer = previouslyOfferedPrice + incrementValue;
        return Math.min(currentSellerOffer, newOffer);
    }

    @Override
    public AID makeDecision(Map<AID, SellerOfferInfo> offers, Buyer buyer) {
        AID bestDecision = null;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<AID, SellerOfferInfo> entry : offers.entrySet()) {
            SellerOfferInfo offer = entry.getValue();
            
            // Lower is better
            // More credibility -> Lower cost
            // More rounds -> Higher cost

            float perceivedOfferCost = offer.getOfferedPrice();
            buyer.logger().info(String.format("!%s evaluated %s from %s as %f",buyer.getLocalName(), offer, entry.getKey().getLocalName(), perceivedOfferCost));

            if(perceivedOfferCost < bestValue){
                bestValue = perceivedOfferCost;
                bestDecision = entry.getKey();
            }
        }

        return bestDecision;
    }
}
