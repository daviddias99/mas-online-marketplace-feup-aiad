package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import olx.agents.Seller;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.engine.ScheduleBase;

public class CredibilityHistogram {
    private Histogram histogram;
    private List<Seller> sellers;

    public CredibilityHistogram(Repast3Launcher launcher, List<Seller> sellers){
        this.sellers = new ArrayList<>();

        long time = System.currentTimeMillis();

        if (this.histogram != null) 
            this.histogram.dispose();

        File dir = new File("analysis/csv/credibility/");
        if (!dir.exists())
            dir.mkdirs();
    
        this.histogram = new MyHistogram("Credibility Distribution", 10, 0, 100, launcher, dir.getPath() + "/" + time + ".csv");

        this.addSellers(sellers);
        this.histogram.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.histogram, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/credibility/");
        if (!dir2.exists())
            dir2.mkdirs();

        this.histogram.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.histogram, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.histogram, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.histogram, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.histogram, "writeToFile");
    }

    public void addSellers(List<Seller> sellers){
        this.sellers.addAll(sellers);
        this.updateHistogram();
    }

    public void updateHistogram(){
        this.histogram.createHistogramItem("Credibility", this.sellers, "getCredibility", -1, 0);
    }
    
}
