package olx.draw;

import java.util.ArrayList;
import java.util.List;

import olx.agents.Seller;
import olx.utils.MyAverageSequence;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;

public class ScamPlot {
    private OpenSequenceGraph plot;
    private ArrayList<Seller> scam_u25;
    private ArrayList<Seller> scam_u50;
    private ArrayList<Seller> scam_u75;
    private ArrayList<Seller> scam_u100;


    public ScamPlot(Repast3Launcher launcher, List<Seller> sellers){
        this.scam_u25 = new ArrayList<>();
        this.scam_u50 = new ArrayList<>();
        this.scam_u75 = new ArrayList<>();
        this.scam_u100 = new ArrayList<>();

        if (this.plot != null) 
            this.plot.dispose();

        this.plot = new OpenSequenceGraph("Scam Analysis", launcher); 
        this.plot.setAxisTitles("time","money earned");

        this.addSellers(sellers);
        this.plot.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.plot, "step", ScheduleBase.LAST);
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
        this.plot.addSequence("Scam ≤ 25" , new MyAverageSequence(this.scam_u25, "getWealth")); 
        this.plot.addSequence("Scam ≤ 50" , new MyAverageSequence(this.scam_u50, "getWealth")); 
        this.plot.addSequence("Scam ≤ 75" , new MyAverageSequence(this.scam_u75, "getWealth")); 
        this.plot.addSequence("Scam ≤ 100" , new MyAverageSequence(this.scam_u100, "getWealth")); 
    }
}
