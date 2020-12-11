package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import olx.agents.Buyer;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.agents.strategies.counter_offer.RelativeTFTCounterOfferStrategy;
import olx.agents.strategies.counter_offer.SmartCounterOfferStrategy;
import olx.utils.MyAverageSequence;
import olx.utils.Util;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
import uchicago.src.sim.analysis.plot.OpenGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class BuyerStratPlot {
    private static int id = 0;
    private OpenSequenceGraph plot;
    private ArrayList<Buyer> smart;
    private ArrayList<Buyer> absTFT;
    private ArrayList<Buyer> relTFT;
    private static final String METHOD = "getMoneySpent";

    public BuyerStratPlot(Repast3Launcher launcher, List<Buyer> buyers){
        BuyerStratPlot.id++;
        this.smart = new ArrayList<>();
        this.absTFT = new ArrayList<>();
        this.relTFT = new ArrayList<>();

        long time = System.currentTimeMillis();

        if (this.plot != null) 
            this.plot.dispose();

        // TODO: maybe put the variables in the name?
        File dir = new File("analysis/csv/buyer_strat/");
        if (!dir.exists())
            dir.mkdirs();

        this.plot = new OpenSequenceGraph("Counter Offer Strategy Analysis " + BuyerStratPlot.id, launcher, dir.getPath() + "/" + time + ".csv", PlotModel.CSV);
        this.plot.setAxisTitles("time","money spent");

        this.addBuyers(buyers);
        this.setPlot();
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/buyer_strat/");
        if (!dir2.exists())
            dir2.mkdirs();

        this.plot.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "writeToFile");
    }

    public void addBuyers(List<Buyer> buyers){
        for(int i = 0; i < buyers.size(); i++)
            if(buyers.get(i).getCounterOfferStrategy() instanceof SmartCounterOfferStrategy)
                this.smart.add(buyers.get(i));
            else if(buyers.get(i).getCounterOfferStrategy() instanceof RelativeTFTCounterOfferStrategy)
                this.relTFT.add(buyers.get(i));
            else
                this.absTFT.add(buyers.get(i));

    }

    public void setPlot(){
        this.plot.addSequence("Smart", new MyAverageSequence(this.smart, METHOD), Util.getBuyerColor(CounterOfferStrategy.Type.SMART), OpenGraph.FILLED_CIRCLE);
        this.plot.addSequence("Relative TFT" , new MyAverageSequence(this.relTFT, METHOD), Util.getBuyerColor(CounterOfferStrategy.Type.RELTFT), OpenGraph.FILLED_CIRCLE);
        this.plot.addSequence("Absolute TFT" , new MyAverageSequence(this.absTFT, METHOD), Util.getBuyerColor(CounterOfferStrategy.Type.ABSTFT), OpenGraph.FILLED_CIRCLE);
    }

    public void close() {
        this.plot.dispose();
    }
}
