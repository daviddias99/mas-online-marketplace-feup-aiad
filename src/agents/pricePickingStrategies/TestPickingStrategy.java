package agents.pricePickingStrategies;

import java.util.List;
import java.util.Random;

import agents.Seller;
import models.Product;
import utils.Util;

public class TestPickingStrategy extends PricePickingStrategy {

    static Random rng;

    @Override
    public float calculateInitialPrice(Seller s, Product p, List<Float> marketPrices) {
        
        float marketAverage = this.calculateAverage(marketPrices);
        return Util.round(marketAverage  * Float.min(0.9f, Util.getNormalRandom(0.7f, 0.3f)), 2) ;
    }

    @Override
    public float calculateInitialPrice(Seller s, Product p) {
        return  Util.round(p.getOriginalPrice()  * Float.min(0.9f, Util.getNormalRandom(0.7f, 0.3f)),2);
    }

    private float calculateAverage(List<Float> marketPrices){

        if(marketPrices.isEmpty())
            return  -1.0f;

        float acc = 0;

        for(Float value : marketPrices){
            acc += value;
        }

        return acc/marketPrices.size();
    }


}
