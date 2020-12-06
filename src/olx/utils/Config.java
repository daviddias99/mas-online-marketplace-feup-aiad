package olx.utils;

import java.util.List;

import olx.agents.Buyer;
import olx.agents.Seller;
import olx.models.Product;

public interface Config {
  public List<Product> getProducts();
  public List<Buyer> getBuyers();
  public List<Seller> getSellers();
}
