package olx.agents.strategies.offer;

import olx.agents.Seller;
import olx.models.OfferInfo;
import olx.models.SellerOfferInfo;
import olx.utils.Util;

public class SmartOfferStrategy extends OfferStrategy {

	@Override
	public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, SellerOfferInfo ownPreviousOffer, Seller seller) {

        float sellerProductPrice = seller.getProductPrice(currentOffer.getProduct().getName());
        float minPrice = sellerProductPrice * (100 -  seller.getElasticity())/100.0f;

		if(previousOffer == null) {
			return sellerProductPrice;
		}
		else {
			float previouslyOfferedPrice = ownPreviousOffer.getOfferedPrice();
			float currentSellerOffer = currentOffer.getOfferedPrice();
			float intervalFraction = Util.randomBetween(3, 5);
			float proposedVariance = (previouslyOfferedPrice - currentSellerOffer)/intervalFraction;
			float adjustedVariance = this.getVariance(currentOffer.getProduct(), proposedVariance);
			return Math.max(currentSellerOffer ,Math.max(Util.round(previouslyOfferedPrice - adjustedVariance, 1), minPrice));
		}
	}

}
