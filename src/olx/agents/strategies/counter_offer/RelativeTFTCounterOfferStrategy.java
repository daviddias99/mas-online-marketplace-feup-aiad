package olx.agents.strategies.counter_offer;

import java.util.Map;

import olx.agents.Buyer;
import jade.core.AID;
import olx.models.OfferInfo;
import olx.models.SellerOfferInfo;
import olx.utils.Util;

public class RelativeTFTCounterOfferStrategy extends CounterOfferStrategy {


    @Override
    protected float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer) {

        if(ownPreviousOffer == null){
            return Util.round(0.5f * offer.getOfferedPrice(), 2);
        }

        float previouslyOfferedPrice = ownPreviousOffer.getOfferedPrice();
        float currentSellerOffer = offer.getOfferedPrice();
        float incrementValue = Math.max(previouslyOfferedPrice * 0.1f, 1.0f);
        float newOffer = previouslyOfferedPrice + incrementValue;
        return Math.min(currentSellerOffer, newOffer);
    }

    @Override
    public AID makeDecision(Map<AID, SellerOfferInfo> offers, Buyer buyer, StringBuilder sb) {
        AID bestDecision = null;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<AID, SellerOfferInfo> entry : offers.entrySet()) {
            SellerOfferInfo offer = entry.getValue();
            
            // Lower is better
            float perceivedOfferCost = offer.getOfferedPrice();
            sb.append(String.format("%n - %s from %s evaluated as %f", offer, entry.getKey().getLocalName(), perceivedOfferCost));

            if(perceivedOfferCost < bestValue){
                bestValue = perceivedOfferCost;
                bestDecision = entry.getKey();
            }
        }

        return bestDecision;
    }
}
