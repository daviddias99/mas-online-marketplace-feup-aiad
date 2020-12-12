package olx.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import olx.models.Product;
import olx.models.ProductSold;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.engine.ScheduleBase;

public class ProductPriceHistogram {
    private static int id = 0;
    private Histogram histogram;
    private Product product;
    private List<ProductSold> productPrices;

    public ProductPriceHistogram(Repast3Launcher launcher, ProductSold productSold) {
        ProductPriceHistogram.id++;
        this.productPrices = new ArrayList<>();
        this.productPrices.add(productSold);
        this.product = productSold.getProduct();

        long time = System.currentTimeMillis();

        if (this.histogram != null)
            this.histogram.dispose();

        File dir = new File("analysis/csv/product/" + this.product.getName() + "/");
        if (!dir.exists())
            dir.mkdirs();

        String histTitle = "Price distribution of " + this.product.getName() + " " + ProductPriceHistogram.id;
        this.histogram = new MyHistogram(histTitle,10, 0, product.getOriginalPrice(), launcher, dir.getPath() + "/" + time + ".csv");

        this.histogram.createHistogramItem("Price", this.productPrices, "getPrice", -1, 0);
        this.histogram.display();

        launcher.getSchedule().scheduleActionAtInterval(100, this.histogram, "step", ScheduleBase.LAST);

        // TODO: maybe put the variables in the name?
        File dir2 = new File("analysis/snapshots/product/" + this.product.getName() + "/");
        if (!dir2.exists())
            dir2.mkdirs();

        this.histogram.setSnapshotFileName(dir2.getPath() + "/" + time + "_");
        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.histogram, "takeSnapshot", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.histogram, "takeSnapshot");

        launcher.getSchedule().scheduleActionAtInterval(Math.pow(10, 5), this.histogram, "writeToFile", ScheduleBase.LAST);
        launcher.getSchedule().scheduleActionAtEnd(this.histogram, "writeToFile");
    }

    public void addProductSold(ProductSold productSold){
        this.productPrices.add(productSold);
    }

    public void close() {
        this.histogram.dispose();
    }
}
