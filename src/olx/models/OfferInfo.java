package olx.models;

import java.io.Serializable;
import java.util.Objects;

public class OfferInfo implements Serializable, Comparable<OfferInfo> {
    private static final long serialVersionUID = 1L;
    
    protected Product product;
    protected Float offeredPrice;
    private int round;

    public OfferInfo(Product product, float offeredPrice) {
        this.product = product;
        this.offeredPrice = offeredPrice;
    }
    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Product getProduct() {
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
        return this.product.equals(oi.getProduct()) && this.offeredPrice == oi.getOfferedPrice();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.product, this.offeredPrice);
    }


    @Override
    public int compareTo(OfferInfo arg0) {
        return this.offeredPrice.compareTo(arg0.offeredPrice);
    }


}
