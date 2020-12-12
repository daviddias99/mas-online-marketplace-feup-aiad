package olx.draw;

import java.util.Arrays;
import java.util.List;

import olx.agents.Seller;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.agents.strategies.offer.RelativeTFTOfferStrategy;
import olx.agents.strategies.offer.SmartOfferStrategy;
import olx.utils.Util;
import sajas.sim.repast3.Repast3Launcher;

public class SellerStratPlot extends StratPlot<Seller> {
    private static int id = 0;
    public SellerStratPlot(Repast3Launcher launcher, List<Seller> buyers){
        super(launcher, buyers, "getWealth", "seller_strat", "Offer Strategy Analysis " + ++id,
            Arrays.asList(
                Util.getBuyerColor(CounterOfferStrategy.Type.SMART), 
                Util.getBuyerColor(CounterOfferStrategy.Type.RELTFT), 
                Util.getBuyerColor(CounterOfferStrategy.Type.ABSTFT)
            ));
    }

    @Override
    public void addAgents(List<Seller> sellers){
        for(int i = 0; i < sellers.size(); i++)
            if(sellers.get(i).getOfferStrategy() instanceof SmartOfferStrategy)
                this.smart.add(sellers.get(i));
            else if(sellers.get(i).getOfferStrategy() instanceof RelativeTFTOfferStrategy)
                this.relTFT.add(sellers.get(i));
            else
                this.absTFT.add(sellers.get(i));

    }
}
