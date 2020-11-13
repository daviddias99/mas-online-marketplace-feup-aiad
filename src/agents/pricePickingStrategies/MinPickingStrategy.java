package agents.pricePickingStrategies;

import agents.Seller;
import models.Product;
import utils.Util;

import java.util.List;

public class MinPickingStrategy extends PricePickingStrategy {
    @Override
    public float calculateInitialPrice(Seller s, Product p, List<Float> marketPrices) {
        return Util.min(marketPrices);
    }

    @Override
    public float calculateInitialPrice(Seller s, Product p) {
        return p.getOriginalPrice();
    }
}
