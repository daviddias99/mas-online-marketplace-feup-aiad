package agents.strategies.offer;

import agents.Seller;
import models.OfferInfo;
import models.SellerOfferInfo;
import utils.Util;

public class RandomAbsoluteTFTOfferStrategy extends OfferStrategy {

    @Override
    public float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, SellerOfferInfo ownPreviousOffer,
            Seller seller) {
        
        float sellerProductPrice = seller.getProductPrice(currentOffer.getProduct().getName());
        float minPrice = sellerProductPrice * (100 -  seller.getElasticity())/100.0f;

		if(previousOffer == null) {
			return sellerProductPrice;
		}
		else {
			float previouslyOfferedPrice = ownPreviousOffer.getOfferedPrice();
            float currentBuyerOffer = currentOffer.getOfferedPrice();
            float baseDecrement = Math.max(sellerProductPrice * 0.1f, 1.0f);
            float decrementValue = baseDecrement +  Util.randomFloatBetween(-baseDecrement/2, baseDecrement/2);
            float newOffer = previouslyOfferedPrice - decrementValue;
            return  Math.max(currentBuyerOffer,Math.max(minPrice, newOffer));  
        }   
    }
}
