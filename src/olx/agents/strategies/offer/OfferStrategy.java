package olx.agents.strategies.offer;

import olx.agents.Seller;
import jade.util.leap.Serializable;
import olx.models.OfferInfo;
import olx.models.Product;
import olx.models.SellerOfferInfo;

/**
 * An offer strategy is used by sellers to calculate the offer to give to a buyer based
 * on the previous offers. The chooseOffer method calculates the new price using it's past offer
 * the buyer's previous offer and current offer. The getVariance method is used to normalize offered
 * values to use realistic values (e.g. increments of 10$ in a 100$ product). The chooseOffer must
 * assure that the offer is smaller than it's previous offer.
 */
public abstract class OfferStrategy implements Serializable{
    private static final long serialVersionUID = 1L;

    public abstract float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, SellerOfferInfo ownPreviousOffer,
            Seller seller);

    protected final float getVariance(Product p ,float proposed){
        return Math.max(Math.max(0.05f * p.getOriginalPrice(),0.5f), proposed);
    }
}
