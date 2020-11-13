package models;

import java.io.Serializable;

public class OfferInfo implements Serializable, Comparable<OfferInfo>{

    protected Product product;
    protected Float offeredPrice;

    public OfferInfo(Product product, float offeredPrice){
        this.product = product;
        this.offeredPrice = offeredPrice;
    }
 
    public Product getProduct(){
        return product;
    }

    public Float getOfferedPrice(){
        return offeredPrice;
    }

    @Override
    public String toString() {
        return String.format("%s at %.2f$",product.toString(),offeredPrice);
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


    @Override
    public int compareTo(OfferInfo arg0) {
        // TODO Auto-generated method stub
        return this.offeredPrice.compareTo(arg0.offeredPrice);
    }


}
