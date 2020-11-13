package agents.offerStrategies;

import jade.util.leap.Serializable;
import models.OfferInfo;

public abstract class OfferStrategy implements Serializable{
    public abstract float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, float initialPrice);
}
