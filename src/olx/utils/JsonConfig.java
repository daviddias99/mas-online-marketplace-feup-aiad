package olx.utils;

import olx.agents.Buyer;
import olx.agents.Seller;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.models.Product;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfig extends Config{
    private static String file;
    private final Buyer[] buyers;
    private final Product[] products;
    private final Seller[] sellers;
    private Map<CounterOfferStrategy.Type, Integer> buyerStrategies;

    JsonConfig(
        @JsonProperty("products") Product[] products,
        @JsonProperty("buyers") Buyer[] buyers,
        @JsonProperty("wavesBuyers") int nWavesBuyers,
        @JsonProperty("periodBuyers") long periodBuyers,
        @JsonProperty("sellers") Seller[] sellers) {
        super(nWavesBuyers, periodBuyers);
        this.products = products;
        this.buyers = buyers;
        this.sellers = sellers;
        this.fillBuyerStrategies();
    }

    public static JsonConfig read(String path) throws IOException {
        File file = new File(path);
        JsonConfig.file = path;

        ObjectMapper objectMapper;
        if (path.contains("json")) {
            objectMapper = new ObjectMapper();
        } else {
            objectMapper = new ObjectMapper(new YAMLFactory());
        }

        return objectMapper.readValue(file, JsonConfig.class);
    }

    public List<Product> getProducts() {
        return Arrays.asList(products.clone());
    }

    public List<Buyer>  getBuyers() {
        return Arrays.asList(buyers.clone());
    }

    public List<Seller>  getSellers() {
        return Arrays.asList(sellers.clone());
    }

    private void fillBuyerStrategies() {
        this.buyerStrategies = new HashMap<>();
        int nextStrategyID = 0;

        for (Buyer buyer : this.buyers) {
            CounterOfferStrategy.Type buyerStrategy = buyer.getCounterOfferStrategy().getType();

            if (! this.buyerStrategies.containsKey(buyerStrategy)) {
                this.buyerStrategies.put(buyerStrategy, nextStrategyID);
                nextStrategyID++;
            }
        }
    }

    @Override
    public Map<CounterOfferStrategy.Type, Integer> getBuyerStrategies() {
        return this.buyerStrategies;
    }

    @Override
    public String toString() {
        return "Config{" +
                "\n  buyers=" + Arrays.toString(buyers) +
                ",\n  products=" + Arrays.toString(products) +
                ",\n  sellers=" + Arrays.toString(sellers) +
                "\n}";
    }

    @Override
    public Config readSelf(Config conf) {
        try {
            return ((JsonConfig) conf).read(JsonConfig.file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
