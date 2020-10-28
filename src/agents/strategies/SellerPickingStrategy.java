package src.agents.strategies;

import java.util.HashMap;

import jade.core.AID;
import jade.util.leap.Serializable;
import src.models.SellerOfferInfo;

public abstract class SellerPickingStrategy implements Serializable{

    public abstract AID pickSeller(HashMap<AID,SellerOfferInfo> offers);
}
