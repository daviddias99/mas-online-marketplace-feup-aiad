package olx.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.analysis.StatisticUtilities;
import uchicago.src.sim.engine.ActionUtilities;
import uchicago.src.sim.util.SimUtilities;


/**
 * Based  on Average Sequence but without error in case of empty list
 */
public class MyAverageSequence implements Sequence {
    private Method m;
    private ArrayList list;

    public MyAverageSequence(ArrayList var1, String var2) {
        try {
            if(!var1.listIterator().hasNext())
                this.m = null;
            else{
                this.m = ActionUtilities.getNoArgMethod(var1.listIterator().next(), var2);
                this.list = var1;
            }
        } catch (NoSuchMethodException var4) {
            SimUtilities.showError("Unable to find method " + var2, var4);
            var4.printStackTrace();
        }

    }

    public double getSValue() {
        if(this.m != null)
            return StatisticUtilities.getAverage(this.list, this.m);
        return 0;
    }
}