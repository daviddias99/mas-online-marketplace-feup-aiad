package olx.models;

public class SellerOfferInfo extends OfferInfo {
    private static final long serialVersionUID = 1L;
    
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
        return String.format("%s at %.2f$ (with %d credibility)", product.toString(), offeredPrice, this.sellerCredibility);
    }
}
