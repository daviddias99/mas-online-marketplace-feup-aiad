package olx.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.Objects;

@JsonIdentityInfo(scope=Product.class, generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private float originalPrice = 0;

    public Product(String name) {
        this.name = name;
    }

    @JsonCreator
    public Product(@JsonProperty("name") String name, @JsonProperty("price") int originalPrice) {
        this(name);
        this.originalPrice = originalPrice;
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
        return String.format("%s:%.2f", getName(), getOriginalPrice());
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
