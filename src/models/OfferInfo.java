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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        OfferInfo oi = (OfferInfo) o;
        // TODO: confirmar == em não objetos
        return this.product.equals(oi.getProduct()) && this.offeredPrice == oi.getOfferedPrice();
    }
}
