package models;

public class SellerOfferInfo extends OfferInfo {

    private int sellerCredibility;

    public SellerOfferInfo(Product product, float offeredPrice, int sellerCredibility){
        super(product, offeredPrice);
        this.sellerCredibility = sellerCredibility;
    }
 
    public int getSellerCredibility(){
        return sellerCredibility;
    }

    @Override
    public String toString() {
        return String.format("%s at %f$ (with %d credibility)", product.toString(), offeredPrice, this.sellerCredibility);
    }
}
