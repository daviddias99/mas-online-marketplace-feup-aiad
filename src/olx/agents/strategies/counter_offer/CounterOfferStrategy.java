package olx.agents.strategies.counter_offer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.awt.Color;

import olx.agents.Buyer;
import jade.core.AID;
import jade.util.leap.Serializable;
import olx.models.OfferInfo;
import olx.models.Product;
import olx.models.SellerOfferInfo;
import olx.utils.Util;

/**
 * An counter offer strategy is used by buyers to calculate the counter offers to the sellers
 * offers (counterPrice), to pick the offers which need to be countered (pickOffers) and to choose
 * the best offer from a collection of offers (makeDecision). The pickOffers signals the problem that
 * all negotiations are ended by returning an empty Map. The counterPrice function must assure that
 * the counter offer is of a larger value than the preivous counter offer.
 */
public abstract class CounterOfferStrategy implements Serializable {
    private static final long serialVersionUID = 1L;

    public final Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers,
            Map<AID, SellerOfferInfo> previousOffers, Map<AID, OfferInfo> ownPreviousOffers, int round) {
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
                OfferInfo counterOffer = new OfferInfo(offer.getValue().getProduct(), counterPrice);
                counterOffers.put(offer.getKey(), counterOffer);
                ownPreviousOffers.put(offer.getKey(), counterOffer);
            }
            offer.getValue().setRound(round);
            previousOffers.put(offer.getKey(), offer.getValue());
            // Else the negotiation won't finish
            // We won't add it to the counterOffers because it's not worth it
        }
        
        return counterOffers;
    }

    protected final float getVariance(Product p ,float proposed){
        return Math.max(Math.max(0.05f * p.getOriginalPrice(),1.0f), proposed);
    }

    protected abstract float counterPrice(SellerOfferInfo offer, OfferInfo ownPreviousOffer);

    public abstract AID makeDecision(Map<AID, SellerOfferInfo> offers, Buyer buyer, StringBuilder sb);

    public abstract String getName();

    // TODO: n devia ser aqui mas não estamos em LPOO
    public abstract Color getColor();
}
