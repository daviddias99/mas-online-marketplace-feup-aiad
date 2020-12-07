package olx.draw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.engine.SimModel;
import uchicago.src.sim.util.SimUtilities;

public class MyHistogram extends Histogram {
    protected String statsFileName;
    private Date date = new Date();
    private int lastIndex = -1;

    public MyHistogram(String title, int numBins, double min, double max, SimModel model, String statsFileName) {
        super(title, numBins, min, max, model);
        this.statsFileName = statsFileName;
    }

    public void writeToFile() {
        BufferedWriter var1 = null;

        try {
            if (this.lastIndex == -1) {
                this.renameFile();
                var1 = new BufferedWriter(new FileWriter(this.statsFileName, true));
                String var2 = DateFormat.getDateTimeInstance().format(this.date);
                var1.write(var2 + "\n");
                var1.write(this.model.getPropertiesValues());
                var1.write("\n");
                String var3 = "\"tick\", \"mean\"";
                

                for(int var5 = 0; var5 < this.histList.size(); ++var5) {
                    var3 = var3 + ", \"" + this.histList.get(var5).getClass().getSimpleName() + var5 + "\"";
                }

                var1.write(var3);
                var1.write("\n");
                this.lastIndex = 0;
            }

            if (var1 == null) {
                var1 = new BufferedWriter(new FileWriter(this.statsFileName, true));
            }

            // int var11 = this.getXValCount();
            // for(int var13 = this.lastIndex; var13 < var11; ++var13) {
                // Tick
            StringBuilder var14 = new StringBuilder(String.valueOf(this.model.getTickCount()));
            
            // mean
            double mean = this.histogram.mean();
            var14.append(", ");
            if(!Double.isNaN(mean))
                var14.append(mean);

            for(int var6 = 0; var6 < this.histList.size(); ++var6) {
                var14.append(", ");
                double var7 = this.dataSource.getBinValue(this.histList.get(var6));
                if (!Double.isNaN(var7)) {
                    var14.append(var7);
                }
            }

            var1.write(var14.toString());
            var1.write("\n");
            // }

            var1.flush();
            // this.lastIndex = var11;
            this.lastIndex++;
        } catch (IOException var10) {
            SimUtilities.showError("Unable to write sequence to file", var10);
            var10.printStackTrace();

            try {
                var1.close();
            } catch (Exception var9) {
            }

            System.exit(0);
        }

    }

    protected void renameFile() throws IOException {
        File var1 = new File(this.statsFileName);
        this.statsFileName = var1.getCanonicalPath();
        if (var1.exists()) {
            int var2 = 1;
            String var4 = this.statsFileName;
            String var5 = "";
            if (this.statsFileName.indexOf(".") != -1) {
                int var6 = this.statsFileName.lastIndexOf(".");
                var4 = this.statsFileName.substring(0, var6);
                var5 = this.statsFileName.substring(var6, this.statsFileName.length());
            }

            File var3;
            do {
                var3 = new File(var4 + var2 + var5);
                ++var2;
            } while(var3.exists());

            var1.renameTo(var3);
            var1.delete();
        }
    }    
}
