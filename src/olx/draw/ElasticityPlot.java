package olx.draw;

import java.util.ArrayList;
import java.util.List;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class ElasticityPlot {
    private OpenSequenceGraph plot;

    private ArrayList<Seller> elast_u10;
    private ArrayList<Seller> elast_u20;
    private ArrayList<Seller> elast_u30;

    public ElasticityPlot(Repast3Launcher launcher, List<Seller> sellers){
        this.elast_u10 = new ArrayList<>();
        this.elast_u20 = new ArrayList<>();
        this.elast_u30 = new ArrayList<>();

        if (this.plot != null) 
            this.plot.dispose();

        this.plot = new OpenSequenceGraph("Elasticity Analysis", launcher); 
        this.plot.setAxisTitles("time","money earned");

        this.addSellers(sellers);        
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);
    }

    public void addSellers(List<Seller> sellers) {
        for(int i = 0; i < sellers.size(); i++)
            if(sellers.get(i).getElasticity() <= 10)
                this.elast_u10.add(sellers.get(i));
            else if(sellers.get(i).getElasticity() <= 20)
                this.elast_u20.add(sellers.get(i));
            else if(sellers.get(i).getElasticity() <= 30)
                this.elast_u30.add(sellers.get(i));

        this.updatePlot();
    }

    public void updatePlot() {
        this.plot.addSequence("Elasticity ≤ 10" , new MyAverageSequence(this.elast_u10, "getWealth")); 
        this.plot.addSequence("Elasticity ≤ 20" , new MyAverageSequence(this.elast_u20, "getWealth")); 
        this.plot.addSequence("Elasticity ≤ 30" , new MyAverageSequence(this.elast_u30, "getWealth")); 
    }
}
