package agents.strategies.counter_offer;

import java.util.Map;

import jade.core.AID;
import models.OfferInfo;
import models.SellerOfferInfo;
import utils.Util;

public class TestCounterOfferStrategy extends CounterOfferStrategy {

    @Override
    public AID finalDecision(Map<AID, SellerOfferInfo> offers) {
        
        AID bestDecision = null;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<AID, SellerOfferInfo> entry : offers.entrySet()) {
            SellerOfferInfo offer = entry.getValue();
            
            // The larger the credibility the smaller the value
            float perceivedOfferValue = offer.getOfferedPrice() / offer.getSellerCredibility();

            if(perceivedOfferValue < bestValue){
                bestValue = perceivedOfferValue;
                bestDecision = entry.getKey();
            }
        }

        return bestDecision;
    }

    @Override
    protected float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer) {
        
        if(ownPreviousOffer == null){
            return Util.round(0.5f * offer.getOfferedPrice(), 1);
        }
        else{
            float variance =  this.getVariance(offer.getProduct(),  (offer.getOfferedPrice() - ownPreviousOffer.getOfferedPrice())/4);
            return Math.min(ownPreviousOffer.getOfferedPrice() + variance, offer.getOfferedPrice());
        }
    }
    
}
