package olx;

import java.io.File;
import java.io.IOException;
import java.util.*;
import jade.wrapper.ControllerException;
import sajas.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.SimInit;
import olx.agents.Buyer;
import olx.agents.BuyerLauncher;
import olx.agents.Seller;
import olx.draw.BuyerStratPlot;
import olx.draw.CredibilityHistogram;
import olx.draw.ElasticityPlot;
import olx.draw.OlxNetwork;
import olx.draw.ScamPlot;
import olx.models.Product;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import olx.utils.Config;
import olx.utils.Stats;
import olx.utils.TerminationListener;
import olx.utils.Creator;
import olx.utils.JsonConfig;

public class Olx extends Repast3Launcher implements TerminationListener {
    private Runtime rt;
    private Profile p;
    private ContainerController container;
    private List<Seller> sellers;
    private List<Buyer> buyers;
    private Map<String, Product> products;

    private Set<Agent> runningAgents;
    private boolean mainMode;
    private Config config;
    private boolean kill;
    private static boolean scamAnalysis;
    private static boolean elasticityAnalysis;
    private static boolean buyerStratAnalysis;
    private static boolean credibilityAnalysis;
    public static boolean logging;

    // config contains the arrays of Products, Buyers and Sellers
    public Olx(boolean mainMode, Config config, boolean kill) {
        this.mainMode = mainMode;
        this.config = config;
        this.kill = kill;
    }

    public void start() {
        createSellers();
        try {
            this.container.acceptNewAgent("buyer_waker", new BuyerLauncher(this, 10000)).start();
        } catch (StaleProxyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createSellers() {
        System.out.println("Creating Sellers");
        // Create the sellers. Seller creation is seperated by 1 seconds. Sellers are
        // identified
        // using the id "seller_i"
        if (this.sellers == null) {
            System.out.println("WARNING: no sellers specified");
            return;
        }
        
        for (int j = 0; j < this.sellers.size(); j++) {
            try {
                this.container.acceptNewAgent("seller_" + j, this.sellers.get(j)).start();
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup seller_" + j);
            }
        }
    }

    public void createBuyers() {
        System.out.println("Creating Buyers");
        if (this.buyers == null) {
            System.out.println("WARNING: no buyers specified");
            return;
        }

        for (int j = 0; j < this.buyers.size(); j++) {
            if (kill)
                this.buyers.get(j).setTerminationListener(this);

            this.runningAgents.add(this.buyers.get(j));

            try {
                this.container.acceptNewAgent("buyer_" + j, this.buyers.get(j)).start();
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup buyer_" + j);
            }
        }
    }

    @Override
    protected void launchJADE() {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();

        this.runningAgents = new HashSet<>();

        this.container = mainMode ? rt.createMainContainer(p) : rt.createAgentContainer(p);

        this.products = new HashMap<>();
        if (config.getProducts() != null) {
            List<Product> prov = config.getProducts();
            for (int i = 0; i < prov.size(); i++)
                this.products.put(prov.get(i).getName(), prov.get(i));
        } else {
            System.out.println("WARNING: no products specified");
        }
        
        if (config.getSellers() != null)
            this.sellers = new ArrayList<>(config.getSellers());

        if (config.getBuyers() != null)
            this.buyers = new ArrayList<>(config.getBuyers());

        this.start();
    }

    @Override
    public void begin() {
        super.begin();
        buildAndScheduleDisplay();
    }

    private ElasticityPlot plotElasticy;
    private OlxNetwork olxNetwork;
    private ScamPlot scamPlot;
    private BuyerStratPlot buyerStratPlot;
    private CredibilityHistogram credibilityHistogram;

    private void buildAndScheduleDisplay() {
        this.olxNetwork = new OlxNetwork(this, this.buyers, this.sellers);
        // graph scam
        if(scamAnalysis)
            this.scamPlot = new ScamPlot(this, this.sellers);
        if(elasticityAnalysis)
            this.plotElasticy = new ElasticityPlot(this, this.sellers);
        if(buyerStratAnalysis)
            this.buyerStratPlot = new BuyerStratPlot(this, this.buyers);
        if(credibilityAnalysis)
            this.credibilityHistogram = new CredibilityHistogram(this, this.sellers);
    }

    /**
     * Create the OLX platform.
     *
     * @param args <configPath> <createHasMainContainer>
     * @throws IOException
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Olx").build()
                .description("Modeling a second hand market place using olx.agents.");
        parser.addArgument("--main", "-m").action(Arguments.storeTrue()).help("start olx.agents in new main container");
        parser.addArgument("--kill", "-k").action(Arguments.storeTrue()).help("platform is shutdown after last buyer exits");
        parser.addArgument("--scam", "-s").action(Arguments.storeTrue()).help("perform a scam analysis");
        parser.addArgument("--bstrat", "-bs").action(Arguments.storeTrue()).help("perform a buyer strategy analysis");
        parser.addArgument("--credibility", "-cr").action(Arguments.storeTrue()).help("make a sellers' credibility distribution analysis");
        parser.addArgument("--logger", "-l").action(Arguments.storeTrue()).help("activate logging per agent (files are always created)");
        parser.addArgument("--elasticity", "-e").action(Arguments.storeTrue()).help("perform a elasticity analysis");
        parser.addArgument("--config", "-c").help("file (YAML or JSON) with experiment configuration");
        parser.addArgument("--generator", "-g").help("file (YAML or JSON) with generator configuration");

        
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
        scamAnalysis = parsedArgs.get("scam");
        elasticityAnalysis = parsedArgs.get("elasticity");
        buyerStratAnalysis = parsedArgs.get("bstrat");
        credibilityAnalysis = parsedArgs.get("credibility");
        logging = parsedArgs.get("logger");
        String configPath = parsedArgs.get("config");
        String generatorPath = parsedArgs.get("generator");

        if (configPath == null && generatorPath == null) {
            parser.printHelp();
            System.out.println("\nConfig or generator file  is required.");
            System.exit(-1);
        }

        String confPath = configPath == null ? generatorPath : configPath;

        String configExtension;
        if (confPath.contains(".")) {
            configExtension = confPath.substring(confPath.lastIndexOf('.') + 1);
        } else {
            configExtension = "";
        }

        if (!(configExtension.equals("json") || configExtension.equals("yaml") || configExtension.equals("yml"))) {
            parser.printHelp();
            System.out.println("\nThe configuration file format should be either JSON (.json) or YAML (.yaml, .yml).");
            System.exit(-1);
        }

        if (!new File(confPath).exists()) {
            System.out.println("Configuration file not found.");
            System.exit(-1);
        }

        // Create config object
        Config config = null;
        try {

            if(configPath != null) {
                config = JsonConfig.read(confPath);
            }
            else {
                config = Creator.read(confPath);
            }

            if (config == null) {
                System.out.println("Invalid file.");
                System.exit(-1);
            }

        } catch (IOException e) {
            System.out.println("Error while reading configuration file.");
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        // SAJAS + REPAST
        SimInit init = new SimInit();
        init.setNumRuns(1); // works only in batch mode
        init.loadModel(new Olx(mainMode, config, kill), null, true);
    }

    @Override
    public synchronized void terminated(Agent a) {
        this.runningAgents.remove(a);
        if (this.runningAgents.isEmpty()) {
            System.out.println();
            Stats.printStats();
            System.out.println();

            try {
                this.container.getPlatformController().kill();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }

    // SAJAS + REPAST
    @Override
    public String[] getInitParam() {
        return new String[0];
    }

    @Override
    public String getName() {
        return "MAS 2nd Hand Marketplace";
    }

}