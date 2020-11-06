package agents.filteringStrategies;

import java.util.Map;

import jade.core.AID;
import jade.util.leap.Serializable;
import models.Negotiation;
import models.SellerOfferInfo;

public abstract class SellerFilteringStrategy implements Serializable{

    public abstract Map<AID, SellerOfferInfo> pickSeller(Map<AID, SellerOfferInfo> offers, Map<AID, Negotiation> offerHistory);
}
