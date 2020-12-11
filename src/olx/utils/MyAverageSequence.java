package olx.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.analysis.StatisticUtilities;
import uchicago.src.sim.engine.ActionUtilities;
import uchicago.src.sim.util.SimUtilities;

/**
 * Based on Average Sequence but without error in case of empty list
 */
public class MyAverageSequence implements Sequence {
    private Method m;
    private String methodName;
    private ArrayList list;

    public MyAverageSequence(ArrayList var1, String var2) {
        this.methodName = var2;
        this.list = var1;
        try {
            if (this.list.isEmpty())
                this.m = null;
            else
                this.m = ActionUtilities.getNoArgMethod(this.list.listIterator().next(), this.methodName);
        } catch (NoSuchMethodException var4) {
            SimUtilities.showError("Unable to find method " + var2, var4);
            var4.printStackTrace();
        }

    }

    public double getSValue() {
        if (this.m != null)
            return StatisticUtilities.getAverage(this.list, this.m);
        if (this.list.isEmpty())
            return 0;
        try {
            this.m = ActionUtilities.getNoArgMethod(this.list.listIterator().next(), this.methodName);
            return StatisticUtilities.getAverage(this.list, this.m);
        } catch (NoSuchMethodException e) {
            SimUtilities.showError("Unable to find method " + methodName, e);
            e.printStackTrace();
        }
        return 0;
    }
}