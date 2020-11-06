package agents.strategies;

import java.util.Map;

import jade.core.AID;
import jade.util.leap.Serializable;
import models.SellerOfferInfo;

public abstract class SellerPickingStrategy implements Serializable{

    public abstract AID pickSeller(Map<AID, SellerOfferInfo> offers);
}
