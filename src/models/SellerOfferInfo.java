package models;

import java.io.Serializable;

public class SellerOfferInfo implements Serializable {

    private Product product;
    private float offeredPrice;
    private int sellerCredibility;

    public SellerOfferInfo(Product product, float offeredPrice, int sellerCredibility){
        this.product = product;
        this.offeredPrice = offeredPrice;
        this.sellerCredibility = sellerCredibility;
    }
 
    public Product getProduct(){
        return product;
    }

    public float getOfferedPrice(){
        return offeredPrice;
    }

    public int getSellerCredibility(){
        return sellerCredibility;
    }

    @Override
    public String toString() {
        return product.toString() + " at " + offeredPrice + "$ (with " + sellerCredibility + " credibility)";
    }
}
