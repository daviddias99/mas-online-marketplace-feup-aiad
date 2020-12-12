package olx.draw;

import java.util.Arrays;
import java.util.List;

import olx.agents.Buyer;
import olx.agents.strategies.counter_offer.*;
import olx.utils.Util;
import sajas.sim.repast3.Repast3Launcher;


public class BuyerStratPlot extends StratPlot<Buyer> {
    private static int id = 0;
    public BuyerStratPlot(Repast3Launcher launcher, List<Buyer> buyers){
        super(launcher, buyers, "getMoneySpent", "buyer_strat", "Counter Offer Strategy Analysis " + ++id, "money spent",
            Arrays.asList(
                Util.getBuyerColor(CounterOfferStrategy.Type.SMART), 
                Util.getBuyerColor(CounterOfferStrategy.Type.RELTFT), 
                Util.getBuyerColor(CounterOfferStrategy.Type.ABSTFT)
            ));
    }

    @Override
    public void addAgents(List<Buyer> buyers){
        for(int i = 0; i < buyers.size(); i++)
            if(buyers.get(i).getCounterOfferStrategy() instanceof SmartCounterOfferStrategy)
                this.smart.add(buyers.get(i));
            else if(buyers.get(i).getCounterOfferStrategy() instanceof RelativeTFTCounterOfferStrategy)
                this.relTFT.add(buyers.get(i));
            else
                this.absTFT.add(buyers.get(i));

    }
}
