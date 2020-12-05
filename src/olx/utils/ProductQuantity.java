package olx.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import olx.models.Product;

public class ProductQuantity {
    private Product product;
    private int quantity;

    @JsonCreator
    public ProductQuantity(@JsonProperty("name") Product product, @JsonProperty("quantity") int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}
