package src.models;

public class Product {
    private final String name;
    private final int originalPrice;
    private int marketPrice;

    public Product(String name, int originalPrice, int marketPrice) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.setMarketPrice(marketPrice);
    }

    public int getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(int marketPrice) {
        this.marketPrice = marketPrice;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return getName() + ":" + getOriginalPrice() + ":" + getMarketPrice();
    }    
}
