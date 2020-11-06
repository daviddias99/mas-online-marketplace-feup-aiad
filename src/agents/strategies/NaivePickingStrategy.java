package agents.strategies;

import java.util.Map;

import jade.core.AID;
import models.SellerOfferInfo;

public class NaivePickingStrategy extends SellerPickingStrategy {

    @Override
    public AID pickSeller(Map<AID, SellerOfferInfo> offers) {
        
        AID bestSeller = null;
        Float bestOffer = null; 

        for(AID seller : offers.keySet()){

            SellerOfferInfo currentSeller = offers.get(seller);

            if(bestOffer == null || currentSeller.getOfferedPrice() < bestOffer){
                bestOffer = currentSeller.getOfferedPrice();
                bestSeller = seller;
            }
        }

        return bestSeller;
    }
    
}
