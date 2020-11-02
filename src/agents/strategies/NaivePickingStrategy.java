package agents.strategies;

import java.util.HashMap;

import jade.core.AID;
import models.SellerOfferInfo;

public class NaivePickingStrategy extends SellerPickingStrategy {

    @Override
    public AID pickSeller(HashMap<AID, SellerOfferInfo> offers) {
        
        AID bestSeller = null;
        Float bestOffer = 150000.0f; //TODO: tirar esta jarvardice

        for(AID seller : offers.keySet()){

            SellerOfferInfo currentSeller = offers.get(seller);

            if(currentSeller.getOfferedPrice() < bestOffer){
                bestOffer = currentSeller.getOfferedPrice();
                bestSeller = seller;
            }
        }

        return bestSeller;
    }
    
}
