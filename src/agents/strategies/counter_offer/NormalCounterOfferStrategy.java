package agents.strategies.counter_offer;

import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import jade.core.AID;
import models.OfferInfo;
import models.SellerOfferInfo;

public class NormalCounterOfferStrategy extends CounterOfferStrategy {

    protected float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer) {
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
