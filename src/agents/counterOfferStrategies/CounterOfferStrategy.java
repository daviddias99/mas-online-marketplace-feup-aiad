package agents.counterOfferStrategies;

import java.util.Map;

import jade.core.AID;
import jade.util.leap.Serializable;
import models.Negotiation;
import models.OfferInfo;
import models.SellerOfferInfo;

public abstract class CounterOfferStrategy implements Serializable{

    public abstract Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers, Map<AID, Negotiation> offerHistory);
}
