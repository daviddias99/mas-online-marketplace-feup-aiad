package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

import sajas.core.Agent;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
import uchicago.src.sim.analysis.plot.OpenGraph;
import uchicago.src.sim.engine.ScheduleBase;

public abstract class StratPlot<T extends Agent> {
    private OpenSequenceGraph plot;
    protected ArrayList<T> smart;
    protected ArrayList<T> absTFT;
    protected ArrayList<T> relTFT;
    private String method;

    protected StratPlot(Repast3Launcher launcher, List<T> agents, String method, String folder, String title, List<Color> colors){
        this.smart = new ArrayList<>();
        this.absTFT = new ArrayList<>();
        this.relTFT = new ArrayList<>();
        this.method = method;

        long time = System.currentTimeMillis();

        if (this.plot != null) 
            this.plot.dispose();

        File dir = new File(String.format("analysis/csv/%s/", folder));
        if (!dir.exists())
            dir.mkdirs();

        this.plot = new OpenSequenceGraph(title, launcher, dir.getPath() + "/" + time + ".csv", PlotModel.CSV);
        this.plot.setAxisTitles("time","money spent");

        this.addAgents(agents);
        this.setPlot(colors.get(0), colors.get(1), colors.get(2));
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);

        File dir2 = new File(String.format("analysis/snapshots/%s/", folder));
        if (!dir2.exists())
            dir2.mkdirs();

        this.plot.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.plot, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.plot, "writeToFile");
    }

    public abstract void addAgents(List<T> agent);

    public void setPlot(Color smartColor, Color relTFTcolor, Color absTFTcolor){
        this.plot.addSequence("Smart", new MyAverageSequence(this.smart, method), smartColor, OpenGraph.FILLED_CIRCLE);
        this.plot.addSequence("Relative TFT" , new MyAverageSequence(this.relTFT, method), relTFTcolor, OpenGraph.FILLED_CIRCLE);
        this.plot.addSequence("Absolute TFT" , new MyAverageSequence(this.absTFT, method), absTFTcolor, OpenGraph.FILLED_CIRCLE);
    }

    public void close() {
        this.plot.dispose();
    }
}
