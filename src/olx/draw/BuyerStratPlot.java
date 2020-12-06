package olx.draw;

import java.util.ArrayList;
import java.util.List;

import olx.agents.Buyer;
import olx.agents.strategies.counter_offer.RelativeTFTCounterOfferStrategy;
import olx.agents.strategies.counter_offer.SmartCounterOfferStrategy;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class BuyerStratPlot {
    private OpenSequenceGraph plot;
    private ArrayList<Buyer> smart;
    private ArrayList<Buyer> absTFT;
    private ArrayList<Buyer> relTFT;


    public BuyerStratPlot(Repast3Launcher launcher, List<Buyer> buyers){
        this.smart = new ArrayList<>();
        this.absTFT = new ArrayList<>();
        this.relTFT = new ArrayList<>();

        if (this.plot != null) 
            this.plot.dispose();

        this.plot = new OpenSequenceGraph("Counter Offer Strategy Analysis", launcher); 
        this.plot.setAxisTitles("time","money spent");

        this.addBuyers(buyers);
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);
    }

    public void addBuyers(List<Buyer> buyers){
        for(int i = 0; i < buyers.size(); i++)
            if(buyers.get(i).getCounterOfferStrategy() instanceof SmartCounterOfferStrategy)
                this.smart.add(buyers.get(i));
            else if(buyers.get(i).getCounterOfferStrategy() instanceof RelativeTFTCounterOfferStrategy)
                this.relTFT.add(buyers.get(i));
            else
                this.absTFT.add(buyers.get(i));

        this.updatePlot();
    }

    public void updatePlot(){
        this.plot.addSequence("Smart" , new MyAverageSequence(this.smart, "getMoneySpent")); 
        this.plot.addSequence("Relative TFT" , new MyAverageSequence(this.relTFT, "getMoneySpent")); 
        this.plot.addSequence("Absolute TFT" , new MyAverageSequence(this.absTFT, "getMoneySpent")); 
    }
}
