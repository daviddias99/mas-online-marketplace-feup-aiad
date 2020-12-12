package olx.models;

public class ProductSold {
    private final Product product;
    private final double price;

    public ProductSold(Product product, double price) {
        this.product = product;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public Product getProduct() { return this.product; }

    public String getName() { return this.product.getName(); }
}
