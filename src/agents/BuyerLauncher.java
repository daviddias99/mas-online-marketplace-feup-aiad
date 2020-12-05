package agents;

import olx.Olx;
import sajas.core.Agent;
import sajas.core.behaviours.WakerBehaviour;

public class BuyerLauncher extends Agent{
    
    private Olx olx;
    private long timeout;

    public BuyerLauncher(Olx olx, long timeout){
        this.olx = olx;
        this.timeout = timeout;
    }


    public void createBuyers() {
        this.olx.createBuyers();
    }

    @Override
    protected void setup() {
        this.addBehaviour(new CreateBuyersBehavior(this, this.timeout));
    }

    class CreateBuyersBehavior extends WakerBehaviour{

        private static final long serialVersionUID = 1L;
        BuyerLauncher agent;

        public CreateBuyersBehavior(BuyerLauncher a, long timeout) {
            super(a, timeout);
            this.agent = a;
        }

        @Override
        protected void handleElapsedTimeout() {
            this.agent.createBuyers();
        }
    }


}
