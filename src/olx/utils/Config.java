package olx.utils;

import olx.agents.Buyer;
import olx.agents.Seller;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.models.Product;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Config {
    private int nWavesBuyers;
    private long periodBuyers;

    public Config(
        @JsonProperty("wavesBuyers") int nWavesBuyers,
        @JsonProperty("periodBuyers") long periodBuyers
    ){
        this.nWavesBuyers = nWavesBuyers;
        this.periodBuyers = periodBuyers;
    }

    public abstract List<Product> getProducts();

    public abstract List<Buyer> getBuyers();

    public abstract List<Seller> getSellers();

    public abstract Map<CounterOfferStrategy.Type, Integer> getBuyerStrategies();

    public abstract Config readSelf(Config conf);

    public long getBuyersPeriod() {
        return this.periodBuyers;
    }

    public int getNWavesBuyers() {
        return this.nWavesBuyers;
    }
}
