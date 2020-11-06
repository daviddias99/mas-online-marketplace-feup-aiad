package agents.filteringStrategies;

import java.util.Map;

import jade.core.AID;
import models.Negotiation;
import models.SellerOfferInfo;

public class NaiveFilterStrategy extends SellerFilteringStrategy {


    @Override
	public Map<AID, SellerOfferInfo> pickSeller(Map<AID, SellerOfferInfo> offers, Map<AID, Negotiation> offerHistory) {
		// TODO Auto-generated method stub
		return offers;
	}
    
}
