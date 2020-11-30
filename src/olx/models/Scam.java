package olx.models;

import java.io.Serializable;

public class Scam implements Serializable {
    private final OfferInfo offer;

    public Scam(OfferInfo buyerOffer) {
        this.offer = buyerOffer;
    }

    @Override
    public String toString() {
        return offer.toString() + " SCAM!";
    }

    public OfferInfo getOfferInfo() {
        return this.offer;
    }
}
