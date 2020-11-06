package agents.counterOfferStrategies;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import models.Negotiation;
import models.OfferInfo;
import models.SellerOfferInfo;

public class NaiveCounterOfferStrategy extends CounterOfferStrategy {


	@Override
	public Map<AID, OfferInfo> pickOffers(Map<AID, SellerOfferInfo> offers, Map<AID, Negotiation> offerHistory) {
		Map<AID, OfferInfo> counterOffers = new HashMap<>();

		for (AID agent : offers.keySet()) {
			SellerOfferInfo offer = offers.get(agent);
			counterOffers.put(agent, new OfferInfo(offer.getProduct(),Math.max(0, offer.getOfferedPrice() - 1)));
		}

		return null;
	}
    
}
