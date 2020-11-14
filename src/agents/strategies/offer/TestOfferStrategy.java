package agents.strategies.offer;

import models.OfferInfo;
import utils.Util;


public class TestOfferStrategy extends OfferStrategy {

	@Override
	public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, float initialPrice) {

		if(currentOffer.getOfferedPrice() < initialPrice)
			return initialPrice;

		return previousOffer == null ? initialPrice : Util.round(currentOffer.getOfferedPrice() * 1.1f, 2);
	}
}
