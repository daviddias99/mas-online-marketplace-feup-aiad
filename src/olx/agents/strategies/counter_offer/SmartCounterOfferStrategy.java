package olx.agents.strategies.counter_offer;

import java.util.Map;

import olx.agents.Buyer;
import jade.core.AID;
import olx.models.OfferInfo;
import olx.models.SellerOfferInfo;
import olx.utils.Util;

public class SmartCounterOfferStrategy extends CounterOfferStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public AID makeDecision(Map<AID, SellerOfferInfo> offers, Buyer buyer, StringBuilder sb) {
        
        AID bestDecision = null;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<AID, SellerOfferInfo> entry : offers.entrySet()) {
            SellerOfferInfo offer = entry.getValue();
            
            // Lower is better
            // More credibility -> Lower cost
            // More rounds -> Higher cost

            float patienceDiscount = (float) Math.pow(buyer.getPatience()/100.0f, offer.getRound());
            float perceivedOfferCost = offer.getOfferedPrice() / offer.getSellerCredibility() /patienceDiscount ;
            sb.append(String.format(Util.LIST_FORMAT + " from %s evaluated as %f", offer, entry.getKey().getLocalName(), perceivedOfferCost));

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
