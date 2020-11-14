package agents.strategies.counter_offer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.util.leap.Serializable;
import models.OfferInfo;
import models.Product;
import models.SellerOfferInfo;
import utils.Util;

public abstract class CounterOfferStrategy implements Serializable {

    public final Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers, Map<AID, SellerOfferInfo> previousOffers, Map<AID, OfferInfo> ownPreviousOffers) {
        Map<AID, OfferInfo> counterOffers = new HashMap<>();

        for(Entry<AID, SellerOfferInfo> offer : offers.entrySet()) {
            // If there is no lastOffer (aka first round)  
            // Or (the sellers offer is different from it's previous offer AND it's different from buyer previous offer
            // AND it's different from the offer I'd give now) do a counter
            float counterPrice =  this.counterPrice(offer.getValue(),ownPreviousOffers.get(offer.getKey()));

            if(!previousOffers.containsKey(offer.getKey()) || 
            (!Util.floatEqual(offer.getValue().getOfferedPrice(), previousOffers.get(offer.getKey()).getOfferedPrice())
            && !Util.floatEqual(offer.getValue().getOfferedPrice(), ownPreviousOffers.get(offer.getKey()).getOfferedPrice())
            && !Util.floatEqual(offer.getValue().getOfferedPrice(), counterPrice)))
            {
                // Update with the newOffer
                counterOffers.put(offer.getKey(), new OfferInfo(offer.getValue().getProduct(), counterPrice));
                previousOffers.put(offer.getKey(), offer.getValue());
            }
            // Else the negotiation won't finish
            // We won't add it to the counterOffers because it's not worth it
        }
        
        return counterOffers;
    }

    protected final float getVariance(Product p ,float proposed){
        return Math.max(Math.max(0.05f * p.getOriginalPrice(),0.5f), proposed);
    }

    protected abstract float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer);

    public abstract AID finalDecision(Map<AID, SellerOfferInfo> offers);
}
