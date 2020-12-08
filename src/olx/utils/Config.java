package olx.utils;

import olx.agents.Buyer;
import olx.agents.Seller;
import olx.models.Product;

import java.util.List;
import java.util.Map;

public interface Config {
  public List<Product> getProducts();
  public List<Buyer> getBuyers();
  public List<Seller> getSellers();
  public Map<String, Integer> getBuyerStrategies();
}
