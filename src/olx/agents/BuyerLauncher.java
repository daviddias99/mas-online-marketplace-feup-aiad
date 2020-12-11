package olx.agents;

import olx.Olx;
import sajas.core.Agent;
import sajas.core.behaviours.WakerBehaviour;

public class BuyerLauncher extends Agent{
    
    private Olx olx;
    private long timeout;
    private int nWaves;
    private final long FIRST_TIMEOUT = 10000;

    public BuyerLauncher(Olx olx, long timeout, int nWaves){
        this.olx = olx;
        this.timeout = timeout;
        this.nWaves = nWaves;
    }


    public void createBuyers() {
        this.olx.addBuyers();
    }

    @Override
    protected void setup() {
        long upperLimit = (this.nWaves - 1) * this.timeout + this.FIRST_TIMEOUT;
        for(long i = this.FIRST_TIMEOUT; i <= upperLimit; i += this.timeout){
            System.out.println("Next Wave: " + i);

            this.addBehaviour(new CreateBuyersBehaviour(this, i, i == upperLimit));
        }
    }

    @Override
    public void takeDown() {
        this.olx.terminated(this);
    }

    class CreateBuyersBehaviour extends WakerBehaviour{

        private static final long serialVersionUID = 1L;
        BuyerLauncher agent;
        private boolean lastBehaviour;

        public CreateBuyersBehaviour(BuyerLauncher a, long timeout, boolean lastBehaviour) {
            super(a, timeout);
            this.agent = a;
            this.lastBehaviour = lastBehaviour;
        }

        @Override
        protected void onWake() {
            this.agent.createBuyers();
            if(this.lastBehaviour)
                this.agent.doDelete();
        }
    }


}
