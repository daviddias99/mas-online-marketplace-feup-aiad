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
        return super.toString() + "(with " + sellerCredibility + " credibility)";
    }
}
