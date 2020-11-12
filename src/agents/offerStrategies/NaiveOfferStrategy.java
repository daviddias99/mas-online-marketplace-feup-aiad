package agents.offerStrategies;

import models.OfferInfo;


public class NaiveOfferStrategy extends OfferStrategy {

	@Override
	public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, float initialPrice) {
		return previousOffer == null ? initialPrice : currentOffer.getOfferedPrice() + 1;
	}


}
