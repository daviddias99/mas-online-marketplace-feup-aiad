package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
import uchicago.src.sim.engine.ScheduleBase;

public class ScamPlot {
    private static int id = 0;
    private OpenSequenceGraph plot;
    private ArrayList<Seller> scam_u25;
    private ArrayList<Seller> scam_u50;
    private ArrayList<Seller> scam_u75;
    private ArrayList<Seller> scam_u100;
    private static final String METHOD = "getWealth";

    public ScamPlot(Repast3Launcher launcher, List<Seller> sellers) {
        ScamPlot.id++;
        this.scam_u25 = new ArrayList<>();
        this.scam_u50 = new ArrayList<>();
        this.scam_u75 = new ArrayList<>();
        this.scam_u100 = new ArrayList<>();

        long time = System.currentTimeMillis();

        if (this.plot != null)
            this.plot.dispose();

        // TODO: maybe put the variables in the name?
        File dir = new File("analysis/csv/scam/");
        if (!dir.exists())
            dir.mkdirs();

        this.plot = new OpenSequenceGraph("Scam Analysis " + ScamPlot.id, launcher, dir.getPath() + "/" + time + ".csv", PlotModel.CSV);
        this.plot.setAxisTitles("time", "money earned");

        this.addSellers(sellers);
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/scam/");
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
            if(sellers.get(i).getScamFactor() <= 25)
                this.scam_u25.add(sellers.get(i));
            else if(sellers.get(i).getScamFactor() <= 50)
                this.scam_u50.add(sellers.get(i));
            else if(sellers.get(i).getScamFactor() <= 75)
                this.scam_u75.add(sellers.get(i));
            else
                this.scam_u100.add(sellers.get(i));

        this.updatePlot();
    }

    public void updatePlot(){
        this.plot.addSequence("Scam ≤ 25" , new MyAverageSequence(this.scam_u25, METHOD)); 
        this.plot.addSequence("Scam ≤ 50" , new MyAverageSequence(this.scam_u50, METHOD)); 
        this.plot.addSequence("Scam ≤ 75" , new MyAverageSequence(this.scam_u75, METHOD)); 
        this.plot.addSequence("Scam ≤ 100" , new MyAverageSequence(this.scam_u100, METHOD)); 
    }

    public void close() {
        this.plot.dispose();
    }
}
