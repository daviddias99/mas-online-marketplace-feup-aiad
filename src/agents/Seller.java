package src.agents;

import java.io.IOException;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Seller extends Agent {
    private int orginalPrice;
    private int credibility;
    // TODO: por produtos e tal (refactor do preço)

    public Seller(int price, int credibility) {
        this.orginalPrice = price;
        this.credibility = credibility;
    }

    protected void setup() {
        register();

    }

    protected void takeDown() {
        deregister();
    }

    private void register() {
        ServiceDescription sd = new ServiceDescription();
        // TODO: ver depois por causa dos vários produtos
        sd.setType("seller");
        sd.setName(getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void deregister() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
}
