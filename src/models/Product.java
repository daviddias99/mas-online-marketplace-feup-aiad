package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private final String name;
    private int originalPrice = 0;
    private int marketPrice;

    public Product(String name) {
        this.name = name;
    }

    @JsonCreator
    public Product(@JsonProperty("name") String name, @JsonProperty("price") int originalPrice) {
        this(name);
        this.originalPrice = originalPrice;
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
        // if(getMarketPrice() == 0)
        return getName() + ":" + getOriginalPrice();
        // return getName() + ":" + getOriginalPrice() + ":" + getMarketPrice();
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
