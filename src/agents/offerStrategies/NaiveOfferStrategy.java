package agents.offerStrategies;

import models.OfferInfo;


public class NaiveOfferStrategy extends OfferStrategy {

	@Override
	public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer) {
		
		return currentOffer.getOfferedPrice() + 1;
	}


}
