package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
import uchicago.src.sim.engine.ScheduleBase;

public class ElasticityPlot {
    private static int id = 0;
    private OpenSequenceGraph plot;

    private ArrayList<Seller> elast_u10;
    private ArrayList<Seller> elast_u20;
    private ArrayList<Seller> elast_u30;
    private static final String METHOD = "getWealth";


    public ElasticityPlot(Repast3Launcher launcher, List<Seller> sellers){
        ElasticityPlot.id++;
        this.elast_u10 = new ArrayList<>();
        this.elast_u20 = new ArrayList<>();
        this.elast_u30 = new ArrayList<>();

        long time = System.currentTimeMillis();

        if (this.plot != null) 
            this.plot.dispose();

        // TODO: maybe put the variables in the name?
        File dir = new File("analysis/csv/elasticity/");
        if (!dir.exists())
            dir.mkdirs();

        this.plot = new OpenSequenceGraph("Elasticity Analysis " + ElasticityPlot.id, launcher, dir.getPath() + "/" + time + ".csv", PlotModel.CSV);
        this.plot.setAxisTitles("time","money earned");

        this.addSellers(sellers);     
        this.setPlot();   
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/elasticity/");
        if (!dir2.exists())
            dir2.mkdirs();

        this.plot.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "writeToFile");
    }

    public void addSellers(List<Seller> sellers) {
        for(int i = 0; i < sellers.size(); i++)
            if(sellers.get(i).getElasticity() <= 10)
                this.elast_u10.add(sellers.get(i));
            else if(sellers.get(i).getElasticity() <= 20)
                this.elast_u20.add(sellers.get(i));
            else if(sellers.get(i).getElasticity() <= 30)
                this.elast_u30.add(sellers.get(i));
    }

    public void setPlot() {
        this.plot.addSequence("Elasticity ≤ 10" , new MyAverageSequence(this.elast_u10, METHOD)); 
        this.plot.addSequence("Elasticity ≤ 20" , new MyAverageSequence(this.elast_u20, METHOD)); 
        this.plot.addSequence("Elasticity ≤ 30" , new MyAverageSequence(this.elast_u30, METHOD)); 
    }

    public void close() {
        this.plot.dispose();
    }
}
