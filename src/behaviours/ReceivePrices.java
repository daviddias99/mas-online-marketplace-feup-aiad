package src.behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import src.agents.Buyer;


public class ReceivePrices extends Behaviour{
    private Buyer buyer;
    private boolean done = false;

    // TODO: aqui?
    private final int TIMEOUT = 500;

    public ReceivePrices(Buyer buyer){
        this.buyer = buyer;
    }

    @Override
    public void action() {
        // TODO: esperar até n receber msg por 500 ms?
        ACLMessage msg = this.buyer.blockingReceive(TIMEOUT);
        if(msg != null) {
            // TODO: depois adicionar a algum sítio tipo potencial sellers
            System.out.println(msg);
            
        } else {
            // TODO: ver se é isto
            this.done = true;
        }

        // TODO: ver melhor condição de saída


    }

    @Override
    public boolean done() {
        return this.done;
    }
}
