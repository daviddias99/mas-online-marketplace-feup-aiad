package agents.strategies.price_picking;

import java.io.Serializable;
import java.util.List;
import agents.Seller;
import models.Product;
import models.SellerOfferInfo;

/**
 * A price picking strategy is used by Sellers for defining the price of a product
 * based on the priced used by other Sellers. The calculateInitialPrice function receives
 * the list of prices and setts the price accordingly. An overload exists for when the 
 * seller is the first one selling the product.
 */
public abstract class PricePickingStrategy implements Serializable{

    public abstract float calculateInitialPrice(Seller s, Product p, List<SellerOfferInfo>marketPrices);
    public abstract float calculateInitialPrice(Seller s, Product p);
}