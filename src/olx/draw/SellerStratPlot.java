package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import olx.agents.Seller;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.agents.strategies.offer.RelativeTFTOfferStrategy;
import olx.agents.strategies.offer.SmartOfferStrategy;
import olx.utils.MyAverageSequence;
import olx.utils.Util;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
import uchicago.src.sim.analysis.plot.OpenGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class SellerStratPlot {
    private static int id = 0;
    private OpenSequenceGraph plot;
    private ArrayList<Seller> smart;
    private ArrayList<Seller> absTFT;
    private ArrayList<Seller> relTFT;
    private static final String METHOD = "getWealth";

    public SellerStratPlot(Repast3Launcher launcher, List<Seller> sellers){
        SellerStratPlot.id++;
        this.smart = new ArrayList<>();
        this.absTFT = new ArrayList<>();
        this.relTFT = new ArrayList<>();

        long time = System.currentTimeMillis();

        if (this.plot != null) 
            this.plot.dispose();

        // TODO: maybe put the variables in the name?
        File dir = new File("analysis/csv/seller_strat/");
        if (!dir.exists())
            dir.mkdirs();

        this.plot = new OpenSequenceGraph("Offer Strategy Analysis " + SellerStratPlot.id, launcher, dir.getPath() + "/" + time + ".csv", PlotModel.CSV);
        this.plot.setAxisTitles("time","money earned");

        this.addSellers(sellers);
        this.setPlot();
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/seller_strat/");
        if (!dir2.exists())
            dir2.mkdirs();

        this.plot.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "writeToFile");
    }

    public void addSellers(List<Seller> sellers){
        for(int i = 0; i < sellers.size(); i++)
            if(sellers.get(i).getOfferStrategy() instanceof SmartOfferStrategy)
                this.smart.add(sellers.get(i));
            else if(sellers.get(i).getOfferStrategy() instanceof RelativeTFTOfferStrategy)
                this.relTFT.add(sellers.get(i));
            else
                this.absTFT.add(sellers.get(i));

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
