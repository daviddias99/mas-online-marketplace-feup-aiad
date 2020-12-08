package olx.utils;

import olx.agents.*;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.models.Product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Creator implements Config {

  private List<Seller> sellers;
  private List<Buyer> buyers;
  private List<Product> products;
  private Map<CounterOfferStrategy.Type, Integer> buyerStrategies;

  public Creator(
    @JsonProperty("product") Product product, 
    @JsonProperty("numSellers") int numSellers, 
    @JsonProperty("numBuyers") int numBuyers, 
    @JsonProperty("sellerStock") int productStockSeller, 
    @JsonProperty("buyerStock") int productStockBuyer,
    @JsonProperty("scamFactors") int[] scamFactors,
    @JsonProperty("elasticities") int[] elasticities,
    @JsonProperty("pickingStrategies") String[] pickingStrategies,
    @JsonProperty("offerStrategies") String[] offerStrategies,
    @JsonProperty("counterOfferStrategies") String[] counterOfferStrategies,
    @JsonProperty("patiences") int[] patiences
    ) {
    this.sellers = new ArrayList<>();
    this.buyers = new ArrayList<>();
    this.products = new ArrayList<>();
    this.generate(product, numSellers, numBuyers, productStockSeller, productStockBuyer, scamFactors, elasticities, pickingStrategies, offerStrategies, counterOfferStrategies, patiences);
    this.fillBuyerStrategies(counterOfferStrategies);
  }

  public static Creator read(String path) throws IOException {
    File file = new File(path);

    ObjectMapper objectMapper;
    if (path.contains("json")) {
        objectMapper = new ObjectMapper();
    } else {
        objectMapper = new ObjectMapper(new YAMLFactory());
    }

    return objectMapper.readValue(file, Creator.class);
}

  public List<Product> getProducts() {
    return products;
  }

  public List<Buyer> getBuyers() {
    return buyers;
  }

  public List<Seller> getSellers() {
    return sellers;
  }

  public void generate(
    Product product, 
    int numSellers, 
    int numBuyers, 
    int productStockSeller, 
    int productStockBuyer,
    int[] scamFactors,
    int[] elasticities,
    String[] pickingStrategies,
    String[] offerStrategies,
    String[] counterOfferStrategies,
    int[] patiences
    ) 
    {

    this.products.add(product);

    for (int i = 0; i < numSellers; i++) {
      int currentScamFactor = scamFactors[i/(int)(Math.ceil((float) numSellers/ scamFactors.length))];
      int currentElasticity = elasticities[i/(int)(Math.ceil((float) numSellers/ elasticities.length))];
      String currentPickingStrategy = pickingStrategies[i/(int)(Math.ceil((float) numSellers/ pickingStrategies.length))];
      String currentOfferStrategy = offerStrategies[i/(int)(Math.ceil((float) numSellers/ offerStrategies.length))];

      ProductQuantity productQuantity = new ProductQuantity(product, productStockSeller);
      ProductQuantity[] pQuantities = { productQuantity };
      this.sellers.add(new Seller(pQuantities, currentScamFactor, currentElasticity, currentPickingStrategy, currentOfferStrategy));
    }

    for (int i = 0; i < numBuyers; i++) {
      int currentPatience = patiences[i/(int)(Math.ceil((float) numBuyers/ patiences.length))];
      String currentCounterOfferStrategy = counterOfferStrategies[i/(int)(Math.ceil((float)numBuyers/ counterOfferStrategies.length))];
      
      ProductQuantity productQuantity = new ProductQuantity(product, productStockBuyer);
      ProductQuantity[] pQuantities = { productQuantity };
      this.buyers.add(new Buyer(pQuantities, currentCounterOfferStrategy, currentPatience));
    }
  }

  @Override
  public Map<CounterOfferStrategy.Type, Integer> getBuyerStrategies() {
    return this.buyerStrategies;
  }

  private void fillBuyerStrategies(String[] counterOfferStrategies) {
    this.buyerStrategies = new HashMap<>();
    for (int i = 0; i < counterOfferStrategies.length; i++) {
      this.buyerStrategies.put(CounterOfferStrategy.Type.valueOf(counterOfferStrategies[i]), i);
    }
  }
}
