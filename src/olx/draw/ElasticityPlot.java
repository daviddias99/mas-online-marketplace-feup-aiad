package olx.draw;

import java.util.ArrayList;
import java.util.Map;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class ElasticityPlot {
    private OpenSequenceGraph plot;

    public ElasticityPlot(Repast3Launcher launcher, Map<Integer, ArrayList<Seller>> elasticityMap){
        if (this.plot != null) 
            this.plot.dispose();

        this.plot = new OpenSequenceGraph("Elasticity Analysis", launcher); 
        this.plot.setAxisTitles("time","money earned");
        this.plot.addSequence("Elasticity ≤ 10" , new MyAverageSequence(elasticityMap.get(10), "getWealth")); 
        this.plot.addSequence("Elasticity ≤ 20" , new MyAverageSequence(elasticityMap.get(20), "getWealth")); 
        this.plot.addSequence("Elasticity ≤ 30" , new MyAverageSequence(elasticityMap.get(30), "getWealth")); 
        
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);
    }
}
