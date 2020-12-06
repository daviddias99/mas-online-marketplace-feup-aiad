package olx.draw;

import java.util.ArrayList;
import java.util.Map;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class ScamPlot {
    private OpenSequenceGraph plot;

    public ScamPlot(Repast3Launcher launcher, Map<Integer, ArrayList<Seller>> scamMap){
        if (this.plot != null) 
            this.plot.dispose();

        this.plot = new OpenSequenceGraph("Scam Analysis", launcher); 
        this.plot.setAxisTitles("time","money earned");
        this.plot.addSequence("Scam ≤ 25" , new MyAverageSequence(scamMap.get(25), "getWealth")); 
        this.plot.addSequence("Scam ≤ 50" , new MyAverageSequence(scamMap.get(50), "getWealth")); 
        this.plot.addSequence("Scam ≤ 75" , new MyAverageSequence(scamMap.get(75), "getWealth")); 
        this.plot.addSequence("Scam ≤ 100" , new MyAverageSequence(scamMap.get(100), "getWealth")); 
        
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);
    }
    
}
