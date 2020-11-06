package models;

import java.io.Serializable;

public class OfferInfo implements Serializable {

    private Product product;
    private float offeredPrice;

    public OfferInfo(Product product, float offeredPrice){
        this.product = product;
        this.offeredPrice = offeredPrice;
    }
 
    public Product getProduct(){
        return product;
    }

    public float getOfferedPrice(){
        return offeredPrice;
    }

    @Override
    public String toString() {
        return String.format("%s at %f$%n",product.toString(),offeredPrice);
    }
}
