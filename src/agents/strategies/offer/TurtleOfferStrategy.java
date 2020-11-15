package agents.strategies.offer;

import agents.Seller;
import models.OfferInfo;
import models.SellerOfferInfo;
import utils.Util;

public class TurtleOfferStrategy extends OfferStrategy {

	@Override
	public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, SellerOfferInfo ownPreviousOffer, Seller seller) {

        float sellerProductPrice = seller.getProductPrice(currentOffer.getProduct().getName());
        float minPrice = sellerProductPrice * (100 -  seller.getElasticity())/100.0f;

		if(previousOffer == null) {
			return sellerProductPrice;
		}
		else {
			float variance = this.getVariance(currentOffer.getProduct(), (ownPreviousOffer.getOfferedPrice() - currentOffer.getOfferedPrice())/15);
			return Math.max(currentOffer.getOfferedPrice() ,Math.max(Util.round(ownPreviousOffer.getOfferedPrice() - variance, 1), minPrice));
		}
	}

}
