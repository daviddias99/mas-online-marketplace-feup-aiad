package agents.strategies.counter_offer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.util.leap.Serializable;
import models.OfferInfo;
import models.SellerOfferInfo;

public abstract class CounterOfferStrategy implements Serializable {

    public Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers, Map<AID, SellerOfferInfo> previousOffers) {
        Map<AID, OfferInfo> counterOffers = new HashMap<>();

        for(Entry<AID, SellerOfferInfo> offer : offers.entrySet()) {
            // If there is no lastOffer (aka first round) always do a counter 
            // Or there was a previous offer and the offer is different (it is lower) 
            //and we can still try to negotiate it and update the previousOffers

            if(!previousOffers.containsKey(offer.getKey()) || ( Math.abs(offer.getValue().getOfferedPrice() - previousOffers.get(offer.getKey()).getOfferedPrice()) >= 0.01 )){
                // Update with the newOffer
                float counterPrice =  this.counterPrice(offer.getValue());
                counterOffers.put(offer.getKey(), new OfferInfo(offer.getValue().getProduct(), counterPrice));
                previousOffers.put(offer.getKey(), offer.getValue());
            }
            // Else the New Offer price is the same as before, we know that he won't lower the price
            // We won't add it to the counterOffers because it's not worth it
        }
        
        return counterOffers;
    }

    protected abstract float counterPrice(SellerOfferInfo offer);

    public abstract AID finalDecision(Map<AID, SellerOfferInfo> offers);
}
