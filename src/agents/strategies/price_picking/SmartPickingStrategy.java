package agents.strategies.price_picking;

import java.util.List;

import agents.Seller;
import models.Product;
import utils.Util;
import models.SellerOfferInfo;

public class SmartPickingStrategy extends PricePickingStrategy {

    // If more sellers exist, choose the a value between 65% and 90% of the market
    // average
    @Override
    public float calculateInitialPrice(Seller s, Product p, List<SellerOfferInfo> marketPrices) {
        float marketAverage = this.weightedAverage(marketPrices);
        float a = Float.max(0.65f, Util.getNormalRandom(0.8f, 0.3f));
        return marketAverage < 0 ? this.calculateInitialPrice(s, p) : Util.round(marketAverage * Float.min(0.9f, a), 1);
    }

    // If no other sellers exist, choose a value between 70% and 90% of the original
    // price
    @Override
    public float calculateInitialPrice(Seller s, Product p) {
        float a = Util.randomBetween(70, 90) / 100.0f;
        return Util.round(p.getOriginalPrice() * a, 1);
    }

    private float weightedAverage(List<SellerOfferInfo> marketPrices) {

        float total = 0;
        float totalWeight = 0;

        for (SellerOfferInfo offer : marketPrices) {

            total += offer.getOfferedPrice() * offer.getSellerCredibility();
            totalWeight += offer.getSellerCredibility();
        }

        return totalWeight > 0 ? total/totalWeight : 0;
    }

}
