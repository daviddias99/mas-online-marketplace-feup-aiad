import java.io.File;
import java.io.IOException;
import java.util.*;

import agents.TerminationAgent;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import agents.Buyer;
import agents.Seller;
import models.Product;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.Config;
import utils.Stats;
import utils.TerminationListener;

public class Olx implements TerminationListener {
    private Runtime rt;
    private Profile p;
    private ContainerController container;
    private List<Seller> sellers;
    private List<Buyer> buyers;
    private Map<String, Product> products;

    private Set<Agent> runningAgents;

    // config contains the arrays of Products, Buyers and Sellers
    public Olx(boolean mainMode, Config config) {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();

        this.runningAgents = new HashSet<>();

        if (mainMode)
            this.container = rt.createMainContainer(p);
        else
            this.container = rt.createAgentContainer(p);

        this.products = new HashMap<>();
        if (config.getProducts() != null) {
            Product[] prov = config.getProducts();
            for (int i = 0; i < prov.length; i++)
                this.products.put(prov[i].getName(), prov[i]);
        } else {
            System.out.println("WARNING: no products specified");
        }

        if (config.getSellers() != null) {
            this.sellers = new ArrayList<>(Arrays.asList(config.getSellers()));
        }

        if (config.getBuyers() != null) {
            this.buyers = new ArrayList<>(Arrays.asList(config.getBuyers()));
        }
    }

    public void start(boolean kill) {
        createSellers();
        createBuyers(kill);
    }

    private void createSellers() {
        // Create the sellers. Seller creation is seperated by 1 seconds. Sellers are
        // identified
        // using the id "seller_i"
        if (this.sellers == null) {
            System.out.println("WARNING: no sellers specified");
            return;
        }

        for (int j = 0; j < this.sellers.size(); j++) {

            try {
                Thread.sleep(500);
                this.container.acceptNewAgent("seller_" + j, this.sellers.get(j)).start();
            } catch (StaleProxyException | InterruptedException e) {
                System.out.println("/!\\ Could not setup seller_" + j);
            }
        }
    }

    private void createBuyers(boolean kill) {
        if (this.buyers == null) {
            System.out.println("WARNING: no buyers specified");
            return;
        }

        for (int j = 0; j < this.buyers.size(); j++) {

            if (kill) {
                this.buyers.get(j).setTerminationListener(this);
            }
            this.runningAgents.add(this.buyers.get(j));

            try {
                this.container.acceptNewAgent("buyer_" + j, this.buyers.get(j)).start();
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup buyer_" + j);
            }
        }
    }

    /**
     * Create the OLX platform.
     * 
     * @param args <configPath> <createHasMainContainer>
     * @throws IOException
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Olx").build()
                .description("Modeling a second hand market place using agents.");
        parser.addArgument("--main", "-m")
                .action(Arguments.storeTrue())
                .help("start agents in new main container");
        parser.addArgument("--kill", "-k")
                .action(Arguments.storeTrue())
                .help("platform is shutdown after last buyer exits");
        parser.addArgument("--config", "-c")
                .help("file (YAML or JSON) with experiment configuration");


        Namespace parsedArgs = null;
        try {
            parsedArgs = parser.parseArgs(args);
        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.exit(-1);
        }


        boolean mainMode = parsedArgs.get("main");
        boolean kill = parsedArgs.get("kill");
        String configPath = parsedArgs.get("config");
        if (configPath == null) {
            parser.printHelp();
            System.out.println("\nConfig file is required.");
            System.exit(-1);
        }

        String configExtension;
        if (configPath.contains(".")) {
            configExtension = configPath.substring(configPath.lastIndexOf('.') + 1);
        } else {
            configExtension = "";
        }

        if (!(configExtension.equals("json") || configExtension.equals("yaml") || configExtension.equals("yml"))) {
            parser.printHelp();
            System.out.println("\nThe configuration file format should be either JSON (.json) or YAML (.yaml, .yml).");
            System.exit(-1);
        }


        if (!new File(configPath).exists()) {
            System.out.println("Configuration file not found.");
            System.exit(-1);
        }
        
        // Create config object
        Config config = null;
        try {
            config = Config.read(configPath);
            if (config == null) {
                System.out.println("Invalid configuration file.");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.out.println("Error while reading configuration file.");
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        Olx olx = new Olx(mainMode, config);
        olx.start(kill);
    }

    @Override
    public synchronized void terminated(Agent a) {
        if (a instanceof TerminationAgent) {
            System.out.println();
            Stats.printStats();
            System.out.println();
            return;
        }

        this.runningAgents.remove(a);
        if (this.runningAgents.isEmpty()) {
            this.shutdown();
        }
    }

    public void shutdown() {
        try {
            TerminationAgent a = new TerminationAgent();
            a.setTerminationListener(this);
            this.container.acceptNewAgent("terminator", a).start();
        } catch (StaleProxyException e) {
            System.out.println("Could not setup terminator agent");
        }
    }
}