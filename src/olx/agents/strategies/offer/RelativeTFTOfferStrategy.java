package olx.agents.strategies.offer;

import olx.agents.Seller;
import olx.models.OfferInfo;
import olx.models.SellerOfferInfo;

public class RelativeTFTOfferStrategy extends OfferStrategy {

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
            float currentSellerOffer = currentOffer.getOfferedPrice();
            float decrementValue = Math.max(previouslyOfferedPrice * 0.1f, 1.0f);
            float newOffer = previouslyOfferedPrice - decrementValue;
			return  Math.max(currentSellerOffer,Math.max(minPrice, newOffer));
        }   
    }
}
