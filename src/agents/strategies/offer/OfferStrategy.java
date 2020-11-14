package agents.strategies.offer;

import agents.Seller;
import jade.util.leap.Serializable;
import models.OfferInfo;
import models.Product;
import models.SellerOfferInfo;

public abstract class OfferStrategy implements Serializable{
    public abstract float chooseOffer(OfferInfo currentOffer, OfferInfo previousOffer, SellerOfferInfo ownPreviousOffer, Seller seller);

    protected final float getVariance(Product p ,float proposed){
        return Math.max(Math.max(0.05f * p.getOriginalPrice(),0.5f), proposed);
    }
}
