package models;

import jade.util.leap.Serializable;

public class NegotiationRound implements Serializable{
    private int roundNumber;
    private float sellerOffer;
    private float buyerOffer;
    private boolean lastRound;

    public NegotiationRound(int roundNumber, float sellerOffer, float buyerOffer) {
        this.roundNumber = roundNumber;
        this.sellerOffer = sellerOffer;
        this.buyerOffer = buyerOffer;
        this.lastRound = false;
    }

    public NegotiationRound(int roundNumber, float sellerOffer) {
        this(roundNumber, sellerOffer, -1);
        this.lastRound = true;
    }

    public boolean isLastRound() {
        return lastRound;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public float getSellerOffer() {
        return sellerOffer;
    }

    public float getBuyerOffer() {
        return buyerOffer;
    }

}
