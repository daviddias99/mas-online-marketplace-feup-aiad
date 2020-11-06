package models;

import java.util.LinkedList;
import java.util.List;

import jade.util.leap.Serializable;

public class Negotiation implements Serializable {

    private List<NegotiationRound> rounds;
    private Boolean over;

    public Negotiation() {
        this.over = false;
        this.rounds = new LinkedList<>();
    }

    public List<NegotiationRound> getRounds() {
        return rounds;
    }

    public void addRound(NegotiationRound newRound){
        this.rounds.add(newRound);
    }

    public Boolean isFinished() {
        return over;
    }

    public void finish() {
        this.over = true;
    }

    public int currentRound(){
        return this.rounds.size() + 1;
    }
}
