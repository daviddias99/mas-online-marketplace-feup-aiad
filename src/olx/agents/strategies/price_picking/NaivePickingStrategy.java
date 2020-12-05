package olx.agents.strategies.price_picking;

import java.util.List;

import olx.agents.Seller;
import olx.models.Product;
import olx.utils.Util;
import olx.models.SellerOfferInfo;

public class NaivePickingStrategy extends PricePickingStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public float calculateInitialPrice(Seller s, Product p, List<SellerOfferInfo> marketPrices) {

        float min = Float.MAX_VALUE;

        for(SellerOfferInfo offer : marketPrices){
            if(offer.getOfferedPrice() < min)
                min = offer.getOfferedPrice();
        }

        return 0.9f * min;
    }

    @Override
    public float calculateInitialPrice(Seller s, Product p) {
        float a = Util.randomBetween(70,90)/100.0f;
        return  Util.round(p.getOriginalPrice()  * a,1);
    }
    
}
