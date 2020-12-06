package olx;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.awt.Color;
import jade.wrapper.ControllerException;
import sajas.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.ScheduleBase;
import uchicago.src.sim.engine.SimInit;
import olx.agents.Buyer;
import olx.agents.BuyerLauncher;
import olx.agents.Seller;
import olx.models.Product;
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
import olx.utils.Config;
import olx.utils.MyAverageSequence;
import olx.utils.Stats;
import olx.utils.TerminationListener;
import olx.utils.Util;
import olx.utils.Creator;
import olx.utils.JsonConfig;

public class Olx extends Repast3Launcher implements TerminationListener {
    private Runtime rt;
    private Profile p;
    private ContainerController container;
    private List<Seller> sellers;
    private Map<Integer, ArrayList<Seller>> scamMap;
    private Map<Integer, ArrayList<Seller>> elasticityMap;
    private List<Buyer> buyers;
    private Map<String, Product> products;

    private Set<Agent> runningAgents;
    private boolean mainMode;
    private Config config;
    private boolean kill;
    private boolean scamAnalysis;
    private boolean elasticityAnalysis;

    // config contains the arrays of Products, Buyers and Sellers
    public Olx(boolean mainMode, Config config, boolean kill, boolean scamAnalysis, boolean elasticityAnalysis) {
        this.mainMode = mainMode;
        this.config = config;
        this.kill = kill;
        this.scamAnalysis = scamAnalysis;
        this.elasticityAnalysis = elasticityAnalysis;
        this.scamMap = new HashMap<>();
        this.elasticityMap = new HashMap<>();
    }

    public void start() {
        nodes = new ArrayList<DefaultDrawableNode>();

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


        ArrayList<Seller> scam_u25 = new ArrayList<>();
        ArrayList<Seller> scam_u50 = new ArrayList<>();
        ArrayList<Seller> scam_u75 = new ArrayList<>();
        ArrayList<Seller> scam_u100 = new ArrayList<>();

        ArrayList<Seller> elast_u10 = new ArrayList<>();
        ArrayList<Seller> elast_u20 = new ArrayList<>();
        ArrayList<Seller> elast_u30 = new ArrayList<>();
        
        for (int j = 0; j < this.sellers.size(); j++) {
            ///
            if(this.sellers.get(j).getScamFactor() <= 25)
                scam_u25.add(this.sellers.get(j));
            else if(this.sellers.get(j).getScamFactor() <= 50)
                scam_u50.add(this.sellers.get(j));
            else if(this.sellers.get(j).getScamFactor() <= 75)
                scam_u75.add(this.sellers.get(j));
            else
                scam_u100.add(this.sellers.get(j));
            ///
            if(this.sellers.get(j).getElasticity() <= 10)
                elast_u10.add(this.sellers.get(j));
            else if(this.sellers.get(j).getElasticity() <= 20)
                elast_u20.add(this.sellers.get(j));
            else if(this.sellers.get(j).getElasticity() <= 30)
                elast_u30.add(this.sellers.get(j));

            try {
                Seller seller = this.sellers.get(j);
                this.container.acceptNewAgent("seller_" + j, seller).start();
                DefaultDrawableNode node = generateNode(Util.localNameToLabel("seller_" + j),
                        Util.getSellerColor(seller.getCredibility()), Util.randomBetween(2 * WIDTH / 3, WIDTH - WIDTH / 115),
                        Util.randomBetween(0, HEIGHT - WIDTH / 115));
                nodes.add(node);
                this.sellers.get(j).setNode(node);
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup seller_" + j);
            }
        }

        this.scamMap.put(25, scam_u25);
        this.scamMap.put(50, scam_u50);
        this.scamMap.put(75, scam_u75);
        this.scamMap.put(100, scam_u100);

        this.elasticityMap.put(10, elast_u10);
        this.elasticityMap.put(20, elast_u20);
        this.elasticityMap.put(30, elast_u30);
    }

    public void createBuyers() {
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
                DefaultDrawableNode node = generateNode(Util.localNameToLabel("buyer_" + j), this.buyers.get(j).getCounterOfferStrategy().getColor(),
                        Util.randomBetween(0, WIDTH / 3), Util.randomBetween(0, HEIGHT - WIDTH / 115));
                nodes.add(node);
                this.buyers.get(j).setNode(node);
            } catch (StaleProxyException e) {
                System.out.println("/!\\ Could not setup buyer_" + j);
            }
        }
        this.updateNetwork();
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
            List<Product> prov = config.getProducts();
            for (int i = 0; i < prov.size(); i++)
                this.products.put(prov.get(i).getName(), prov.get(i));
        } else {
            System.out.println("WARNING: no products specified");
        }
        
        if (config.getSellers() != null) {
            this.sellers = new ArrayList<>(config.getSellers());
        }

        if (config.getBuyers() != null) {
            this.buyers = new ArrayList<>(config.getBuyers());
        }

        this.start();
    }

    private static List<DefaultDrawableNode> nodes;

    public static DefaultDrawableNode getNode(String label) {
        for (DefaultDrawableNode node : nodes) {
            if (node.getNodeLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }

    private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        OvalNetworkItem oval = new OvalNetworkItem(x, y);
        oval.allowResizing(false);
        oval.setHeight(WIDTH / 115);
        oval.setWidth(WIDTH / 115);

        DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
        node.setColor(color);
        node.setBorderColor(color);
        node.setBorderWidth(1);

        return node;
    }

    @Override
    public void begin() {
        super.begin();
        buildAndScheduleDisplay();
    }

    private DisplaySurface dsurf;
    private int WIDTH = 1920, HEIGHT = 1080;
    private OpenSequenceGraph plotScam;
    private OpenSequenceGraph plotElasticy;
    private Network2DDisplay network;

    public void updateNetwork() {

        if (this.network != null){
            this.dsurf.removeProbeableDisplayable(this.network);
        }

        this.network = new Network2DDisplay(nodes, WIDTH, HEIGHT);
        this.dsurf.addDisplayableProbeable(this.network, "Network Display" + this.network.hashCode());
        this.dsurf.addZoomable(this.network);
        addSimEventListener(this.dsurf);
    }

    private void buildAndScheduleDisplay() {

        // display surface
        if (this.dsurf != null)
            this.dsurf.dispose();
        this.dsurf = new DisplaySurface(this, "MAS 2nd Hand Marketplace Display");
        registerDisplaySurface("MAS 2nd Hand Marketplace Display", this.dsurf);
        this.updateNetwork();
        this.dsurf.display();

        getSchedule().scheduleActionAtInterval(1, this.dsurf, "updateDisplay", ScheduleBase.LAST);

        // graph scam
        if(this.scamAnalysis){
            if (this.plotScam != null) 
                this.plotScam.dispose();
            this.plotScam = new OpenSequenceGraph("Scam Analysis", this); 
            this.plotScam.setAxisTitles("time","money earned");
            this.plotScam.addSequence("Scam ≤ 25" , new MyAverageSequence(this.scamMap.get(25), "getWealth")); 
            this.plotScam.addSequence("Scam ≤ 50" , new MyAverageSequence(this.scamMap.get(50), "getWealth")); 
            this.plotScam.addSequence("Scam ≤ 75" , new MyAverageSequence(this.scamMap.get(75), "getWealth")); 
            this.plotScam.addSequence("Scam ≤ 100" , new MyAverageSequence(this.scamMap.get(100), "getWealth")); 
            
            this.plotScam.display();

            // TODO: estava só Schedule. ver qual a != vs ScheduleBase
            getSchedule().scheduleActionAtInterval(100, this.plotScam, "step", ScheduleBase.LAST);
        }
        if(this.elasticityAnalysis){
            if (this.plotElasticy != null) 
                this.plotElasticy.dispose();
            this.plotElasticy = new OpenSequenceGraph("Elasticity Analysis", this); 
            this.plotElasticy.setAxisTitles("time","money earned");
            this.plotElasticy.addSequence("Elasticity ≤ 10" , new MyAverageSequence(this.elasticityMap.get(10), "getWealth")); 
            this.plotElasticy.addSequence("Elasticity ≤ 20" , new MyAverageSequence(this.elasticityMap.get(20), "getWealth")); 
            this.plotElasticy.addSequence("Elasticity ≤ 30" , new MyAverageSequence(this.elasticityMap.get(30), "getWealth")); 
            
            this.plotElasticy.display();

            // TODO: estava só Schedule. ver qual a != vs ScheduleBase
            getSchedule().scheduleActionAtInterval(100, this.plotElasticy, "step", ScheduleBase.LAST);
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
                .description("Modeling a second hand market place using olx.agents.");
        parser.addArgument("--main", "-m").action(Arguments.storeTrue()).help("start olx.agents in new main container");
        parser.addArgument("--kill", "-k").action(Arguments.storeTrue()).help("platform is shutdown after last buyer exits");
        parser.addArgument("--scam", "-s").action(Arguments.storeTrue()).help("perform a scam analysis");
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
        boolean scamAnalysis = parsedArgs.get("scam");
        boolean elasticityAnalysis = parsedArgs.get("elasticity");
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
        init.loadModel(new Olx(mainMode, config, kill, scamAnalysis, elasticityAnalysis), null, true);
    }

    @Override
    public synchronized void terminated(Agent a) {
        this.runningAgents.remove(a);
        if (this.runningAgents.isEmpty()) {
            System.out.println();
            Stats.printStats();
            System.out.println();

            this.shutdown();
        }
    }

    public void shutdown() {
        try {
            this.container.getPlatformController().kill();
        } catch (ControllerException e) {
            e.printStackTrace();
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