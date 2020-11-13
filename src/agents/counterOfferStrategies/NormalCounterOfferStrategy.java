package agents.counterOfferStrategies;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import jade.core.AID;
import models.OfferInfo;
import models.SellerOfferInfo;

public class NormalCounterOfferStrategy extends CounterOfferStrategy {

    @Override
    // TODO: ver se ao passar para aqui os Maps isto atualiza (referência mesmo)
    public Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers, Map<AID, SellerOfferInfo> previousOffers) {
        Map<AID, OfferInfo> counterOffers = new HashMap<>();

        for(Entry<AID, SellerOfferInfo> offer : offers.entrySet()) {
            // If ther is no lastOffer (aka first round) always do a counter
            // Or there was a previous offer
            // & the offer is different (it is lower) and we can still try to negotiate it and update the previousOffers
            // TODO: ver se equals do OfferInfo dá overload ao do SellerOfferInfo
            if(!previousOffers.containsKey(offer.getKey()) || !previousOffers.get(offer.getKey()).equals(offer.getValue())){
                // Update with the newOffer
                previousOffers.put(offer.getKey(), offer.getValue());
                counterOffers.put(offer.getKey(), new OfferInfo(offer.getValue().getProduct(), this.counterPrice(offer.getValue())));
            }
            // Else the New Offer price is the same as before, we know that he won't lower the price
            // We won't add it to the counterOffers because it's not worth it
            
        }
        
        return counterOffers;
    }

    protected float counterPrice(SellerOfferInfo offer){
        // TODO: improve price function neste momento acho q n faz muito sentido
        float decrease = (float) Math.abs(offer.getOfferedPrice() - ((new Random()).nextGaussian() * offer.getSellerCredibility() + offer.getOfferedPrice()));
        return offer.getOfferedPrice() - decrease;
    }

    @Override
    public AID finalDecision(Map<AID, SellerOfferInfo> offers) {
        // Tries to get lowest offering price
        float lowestPrice = Float.POSITIVE_INFINITY;
        AID result = null;
        for(Entry<AID, SellerOfferInfo> offer :  offers.entrySet())
            if(offer.getValue().getOfferedPrice() < lowestPrice)
                result = offer.getKey();

        return result;
    }
    
}
