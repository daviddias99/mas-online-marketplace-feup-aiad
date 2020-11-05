import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
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
    private List<Product> products;

    public Olx(boolean mainMode, Config config) {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();

        if (mainMode)
            this.container = rt.createMainContainer(p);
        else
            this.container = rt.createAgentContainer(p);

        this.products = new ArrayList<>(Arrays.asList(config.getProducts()));
        this.sellers = new ArrayList<>(Arrays.asList(config.getSellers()));
        this.buyers = new ArrayList<>(Arrays.asList(config.getBuyers()));

        createSellers();
        createBuyers();
    }

    private void createSellers() {
        int i = 1;

        for (Seller s : this.sellers) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                AgentController ac = this.container.acceptNewAgent("seller_" + i, s);
                ac.start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }
            i++;
        }
    }

    private void createBuyers() {
        int i = 1;
        for (Buyer b : this.buyers) {
            try {
                this.container.acceptNewAgent("buyer_" + i, b).start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            i++;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Expected 2 arguments.");
            System.exit(-1);
        }

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

        Config config = Config.read("config.yaml");
        boolean mainMode = Boolean.parseBoolean(args[1]);

        Olx olx = new Olx(mainMode, config);

        /*try {
            olx.container.kill();
            olx.rt.shutDown();
        } catch (StaleProxyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
}
