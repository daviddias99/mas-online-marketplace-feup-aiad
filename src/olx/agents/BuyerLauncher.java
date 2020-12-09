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
        for(long i = this.FIRST_TIMEOUT; i <= (this.nWaves - 1) * this.timeout + this.FIRST_TIMEOUT; i += this.timeout){
            System.out.println("Next Wave: " + i);

            this.addBehaviour(new CreateBuyersBehavior(this, i));
        }
    }

    @Override
    public void takeDown() {
        this.olx.terminated(this);
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
