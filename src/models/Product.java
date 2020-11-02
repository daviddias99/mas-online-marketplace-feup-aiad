package models;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private final String name;
    private float originalPrice = 0;
    private float marketPrice;

    public Product(String name) {
        this.name = name;
    }

    public Product(String name, float originalPrice) {
        this(name);
        this.originalPrice = originalPrice;
    }

    public float getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(float marketPrice) {
        this.marketPrice = marketPrice;
    }

    public void setOriginalPrice(float originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public float getOriginalPrice() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        Product p = (Product) o;
        return this.name.equals(p.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
