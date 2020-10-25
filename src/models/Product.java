package src.models;

public class Product {
    private final String name;
    private int originalPrice = 0;
    private int marketPrice;

    public Product(String name, int originalPrice) {
        this.name = name;
        this.originalPrice = originalPrice;
    }

    public Product(String name) {
        this.name = name;
    }

    public int getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(int marketPrice) {
        this.marketPrice = marketPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public int getOriginalPrice() {
        return originalPrice;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        if(getOriginalPrice() == 0)
            return getName();
        return getName() + ":" + getOriginalPrice();
    }    
}
