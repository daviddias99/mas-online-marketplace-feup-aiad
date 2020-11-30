import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import agents.TerminationAgent;
import sajas.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import agents.Buyer;
import agents.Seller;
import models.Product;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.Config;
import utils.Stats;
import utils.TerminationListener;
import utils.Util;

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

    // config contains the arrays of Products, Buyers and Sellers
    public Olx(boolean mainMode, Config config, boolean kill) {
        this.mainMode = mainMode;
        this.config = config;
        this.kill = kill;
    }

    public void start() {
        nodes = new ArrayList<DefaultDrawableNode>();

        createSellers();
        createBuyers(this.kill);
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
                DefaultDrawableNode node =
                        generateNode("S" + j, Color.RED,
                                Util.randomBetween(WIDTH/2, WIDTH), Util.randomBetween(0, HEIGHT));
                nodes.add(node);
                this.sellers.get(j).setNode(node);
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup seller_" + j);
            }
        }
    }

    private void createBuyers(boolean kill) {
        System.out.println("Creating Buyers");
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
                DefaultDrawableNode node =
                        generateNode("B" + j, Color.BLUE,
                                Util.randomBetween(0, WIDTH/2), Util.randomBetween(0, HEIGHT));
                nodes.add(node);
                this.buyers.get(j).setNode(node);
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

        // SAJAS + REPAST
        SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new Olx(mainMode, config, kill), null, true);
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
    
    // SAJAS + REPAST
    @Override
    public String[] getInitParam() {
        return new String[0];
    }

    @Override
    public String getName() {
        return "MAS 2nd Hand Marketplace";
    }

    @Override
    protected void launchJADE() {
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

        this.start();
    }

    private static List<DefaultDrawableNode> nodes;

    /*public static DefaultDrawableNode getNode(String label) {
        for(DefaultDrawableNode node : nodes) {
            if(node.getNodeLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }*/

    private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        OvalNetworkItem oval = new OvalNetworkItem(x,y);
        oval.allowResizing(false);
        oval.setHeight(HEIGHT/30);
        oval.setWidth(WIDTH/30);

        DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
        node.setColor(color);

        return node;
    }

    @Override
    public void begin() {
        super.begin();
        buildAndScheduleDisplay();
    }

    private DisplaySurface dsurf;
    private int WIDTH = 800, HEIGHT = 800;
    private OpenSequenceGraph plot;

    private void buildAndScheduleDisplay() {

        // display surface
        if (dsurf != null) dsurf.dispose();
        dsurf = new DisplaySurface(this, "MAS 2nd Hand Marketplace Display");
        registerDisplaySurface("MAS 2nd Hand Marketplace Display", dsurf);
        Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
        dsurf.addDisplayableProbeable(display, "Network Display");
        dsurf.addZoomable(display);
        addSimEventListener(dsurf);
        dsurf.display();

        // graph
        /*
        if (plot != null) plot.dispose();
        plot = new OpenSequenceGraph("Service performance", this);
        plot.setAxisTitles("time", "% successful service executions");

        plot.addSequence("Consumers", new Sequence() {
            public double getSValue() {
                // iterate through consumers
                double v = 0.0;
                for(int i = 0; i < consumers.size(); i++) {
                    v += consumers.get(i).getMovingAverage(10);
                }
                return v / consumers.size();
            }
        });
        plot.addSequence("Filtering Consumers", new Sequence() {
            public double getSValue() {
                // iterate through filtering consumers
                double v = 0.0;
                for(int i = 0; i < filteringConsumers.size(); i++) {
                    v += filteringConsumers.get(i).getMovingAverage(10);
                }
                return v / filteringConsumers.size();
            }
        });
        plot.display();
        */

        getSchedule().scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
        // getSchedule().scheduleActionAtInterval(100, plot, "step", Schedule.LAST);
    }
}