package agents.strategies.price_picking;

import java.io.Serializable;
import java.util.List;
import agents.Seller;
import models.Product;

public abstract class PricePickingStrategy implements Serializable{

    public abstract float calculateInitialPrice(Seller s, Product p, List<Float> marketPrices);
    public abstract float calculateInitialPrice(Seller s, Product p);
}