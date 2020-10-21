package src.agents;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import src.behaviours.AskPrice;
import src.behaviours.ReceivePrices;

public class Buyer extends Agent{
    // TODO: depois por lista de produtos (??)/received
    private boolean success;
    private String product;

    public Buyer(String product){
        this.success = false;
        this.product = product;
    }

    public String getProduct(){
        return this.product;
    }


    protected void setup() {
        // TODO: depois ver se dá para mudar para um que repita ciclicamente até success true
        // se calhar dá para chegar ao final e por reset se success false
        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new AskPrice(this));
        seq.addSubBehaviour(new ReceivePrices(this));
        addBehaviour(seq);
    }
}
