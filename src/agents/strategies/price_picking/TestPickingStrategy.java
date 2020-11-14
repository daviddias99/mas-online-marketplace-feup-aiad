package agents.strategies.price_picking;

import java.util.List;
import java.util.Random;

import agents.Seller;
import models.Product;
import utils.Util;

public class TestPickingStrategy extends PricePickingStrategy {

    static Random rng;

    @Override
    public float calculateInitialPrice(Seller s, Product p, List<Float> marketPrices) {
        float marketAverage = Util.average(marketPrices);
        float a = Util.getNormalRandom(0.7f, 0.3f);
        return Util.round(marketAverage  * Float.min(0.9f,a ), 2) ;
    }

    @Override
    public float calculateInitialPrice(Seller s, Product p) {
        float a = Util.randomBetween(70,90)/100.0f;
        return  Util.round(p.getOriginalPrice()  * a,2);
    }

}
