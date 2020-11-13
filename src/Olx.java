import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import agents.Buyer;
import agents.Seller;
import models.Product;
import utils.Config;

public class Olx {
    public Runtime rt;
    private Profile p;
    public ContainerController container;
    private List<Seller> sellers;
    private List<Buyer> buyers;
    private Map<String, Product> products;

    // config contains the arrays of Products, Buyers and Sellers
    public Olx(boolean mainMode, Config config) {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();

        if (mainMode)
            this.container = rt.createMainContainer(p);
        else
            this.container = rt.createAgentContainer(p);

        this.products = new HashMap<>();
        Product[] prov = config.getProducts();
        for(int i = 0; i < prov.length; i++)
            this.products.put(prov[i].getName(), prov[i]);
        this.sellers = new ArrayList<>(Arrays.asList(config.getSellers()));
        this.buyers = new ArrayList<>(Arrays.asList(config.getBuyers()));

        createSellers();
        createBuyers();
    }

    private void createSellers() {

        // Create the sellers. Seller creation is seperated by 1 seconds. Sellers are identified
        // using the id "seller_i"

        for (int j = 0; j < this.sellers.size(); j++) {
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                this.container.acceptNewAgent("seller_" + j, this.sellers.get(j)).start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }            
    }

    private void createBuyers() {
        for (int j = 0; j < this.buyers.size(); j++) {
            try {
                this.container.acceptNewAgent("buyer_" + j, this.buyers.get(j)).start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Create the OLX platform.
     * @param args <configPath> <createHasMainContainer>
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Expected 2 arguments.");
            System.exit(-1);
        }

        // Get config path
        String configPath = args[0];
        String configExtension;
        if (configPath.contains(".")) {
            configExtension = configPath.substring(configPath.lastIndexOf('.') + 1);
        } else {
            configExtension = "";
        }

        if (!(configExtension.equals("json") || configExtension.equals("yaml") || configExtension.equals("yml"))) {
            System.out.println("The configuration file format should be either JSON (.json) or YAML (.yaml, .yml).");
            System.exit(-1);
        }

        if (!new File(configPath).exists()) {
            System.out.println("Configuration file not found.");
            System.exit(-1);
        }

        // Create config object
        Config config = Config.read(configPath);        
        boolean mainMode = Boolean.parseBoolean(args[1]);
        
        new Olx(mainMode, config);

        // TODO: kill plaform
    }
}
