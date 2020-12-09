package olx;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import olx.draw.NetworkAgent;
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
    public static boolean isFirstRun = true;

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
        super();
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

        if (this.config == null) {
            this.config = this.parseConfigFromParameters();
        }
        else {
            this.config = this.config.readSelf(this.config);
        }

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

    private String[] parseStrategiesFromString(String string) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("(\\w+)").matcher(string);
        while (m.find()) {
            System.out.println(m.group());
            allMatches.add(m.group());
        }
        return allMatches.toArray(new String[0]);
    }

    private int[] parseIntsFromString(String string) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("(\\d+)").matcher(string);
        while (m.find()) {
            System.out.println(m.group());
            allMatches.add(m.group());
        }
        int[] result = new int[allMatches.size()];

        for (int j = 0; j < allMatches.size(); j++) {
            result[j] = Integer.parseInt(allMatches.get(j));
        }

        return result;
    }

    private Config parseConfigFromParameters() {
        return new Creator(new Product(this.PRODUCT_NAME, this.PRODUCT_PRICE), this.NUM_SELLERS, this.NUM_BUYERS,
                this.SELLER_STOCK, this.BUYER_STOCK, this.parseIntsFromString(this.SCAM_FACTORS),
                this.parseIntsFromString(this.ELASTICITIES), this.parseStrategiesFromString(this.PICKING_STRATS),
                this.parseStrategiesFromString(this.OFFER_STRATS), this.parseStrategiesFromString(this.CTOFFER_STRATS),
                this.parseIntsFromString(this.PATIENCES));
    }

    @Override
    public void begin() {
        super.begin();

        // if(Olx.isFirstRun)
        buildAndScheduleDisplay();
        // Olx.isFirstRun = false;
    }

    private ElasticityPlot plotElasticy;
    private OlxNetwork olxNetwork;
    private ScamPlot scamPlot;
    private BuyerStratPlot buyerStratPlot;
    private CredibilityHistogram credibilityHistogram;

    private void buildAndScheduleDisplay() {

        if(this.olxNetwork != null){
            this.olxNetwork.close();
        }
        this.olxNetwork = new OlxNetwork(this, this.buyers, this.sellers, this.config.getBuyerStrategies());
        // graph scam
        if (scamAnalysis){

            if(this.scamPlot != null)
                this.scamPlot.close();

            this.scamPlot = new ScamPlot(this, this.sellers);
        }
        if (elasticityAnalysis){
            if(this.plotElasticy != null)
                this.plotElasticy.close();
    
            this.plotElasticy = new ElasticityPlot(this, this.sellers);
        }
        if (buyerStratAnalysis){
            if(this.buyerStratPlot != null)
                this.buyerStratPlot.close();

            this.buyerStratPlot = new BuyerStratPlot(this, this.buyers);
        }
        if (credibilityAnalysis) {
            if(this.credibilityHistogram != null)
                this.credibilityHistogram.close();

            this.credibilityHistogram = new CredibilityHistogram(this, this.sellers);
        }
    }

    private static Config getConfig(String confPath, ArgumentParser parser, boolean generate) {

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

            if (generate) {
                config = Creator.read(confPath);
            } else {
                config = JsonConfig.read(confPath);
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

        return config;
    }

    /**
     * Create the OLX platform.
     *
     * @param args <configPath> <createHasMainContainer>
     * @throws IOException
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Olx").build().description("Modeling a second hand market place using agents.");
        parser.addArgument("--main", "-m").action(Arguments.storeTrue()).help("start agents in new main container");
        parser.addArgument("--kill", "-k").action(Arguments.storeTrue()).help("platform is shutdown after last buyer exits");
        parser.addArgument("--scam", "-s").action(Arguments.storeTrue()).help("perform a scam analysis");
        parser.addArgument("--batch", "-b").help("Exec in batch mode with X runs (default=1)");
        parser.addArgument("--bstrat", "-bs").action(Arguments.storeTrue()).help("perform a buyer strategy analysis");
        parser.addArgument("--credibility", "-cr").action(Arguments.storeTrue())
                .help("make a sellers' credibility distribution analysis");
        parser.addArgument("--logger", "-l").action(Arguments.storeTrue())
                .help("activate logging per agent (files are always created)");
        parser.addArgument("--elasticity", "-e").action(Arguments.storeTrue()).help("perform a elasticity analysis");
        parser.addArgument("--config", "-c").help("file (YAML or JSON) with experiment configuration");
        parser.addArgument("--generator", "-g").help("file (YAML or JSON) with generator configuration");

        Namespace parsedArgs = null;
        try {
            parsedArgs = parser.parseArgs(args);
        } catch (HelpScreenException e) {
            System.exit(0);
        } catch (ArgumentParserException e) {
            System.out.println(e);
            System.exit(-1);
        }

        boolean mainMode = parsedArgs.get("main");
        boolean kill = parsedArgs.get("kill");
        String batchMode = parsedArgs.get("batch");
        boolean isBatchMode = batchMode != null;
        int numBatches = isBatchMode ? (batchMode.equals("") ? 1 : Integer.parseInt(batchMode)) : 1;

        scamAnalysis = parsedArgs.get("scam");
        elasticityAnalysis = parsedArgs.get("elasticity");
        buyerStratAnalysis = parsedArgs.get("bstrat");
        credibilityAnalysis = parsedArgs.get("credibility");
        logging = parsedArgs.get("logger");
        String configPath = parsedArgs.get("config");
        String generatorPath = parsedArgs.get("generator");
        String confPath = configPath == null ? generatorPath : configPath;

        if (configPath != null && generatorPath != null) {
            System.out.println("Can't use both config and generation");
            System.exit(-1);
        }

        boolean generate = generatorPath != null;
        Config config = (configPath != null || generatorPath != null) ? getConfig(confPath, parser, generate) : null;

        if(config != null && !isBatchMode){
            System.out.println("File configs are meant for batch processing");
            System.exit(-1);  
        }

        // SAJAS + REPAST
        SimInit init = new SimInit();
        init.setNumRuns(numBatches); // works only in batch mode
        init.loadModel(new Olx(mainMode, config, kill), null, isBatchMode);
    }

    @Override
    public synchronized void terminated(Agent a) {
        if(a instanceof NetworkAgent)
            this.olxNetwork.removeNode(((NetworkAgent) a).getNode());
        if(!this.kill)
            return;
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

    private String PRODUCT_NAME = "vinil";
    private int PRODUCT_PRICE = 100;
    private int NUM_SELLERS = 80;
    private int NUM_BUYERS = 80;
    private int SELLER_STOCK = 80;
    private int BUYER_STOCK = 80;
    private String SCAM_FACTORS = "100, 75, 50, 25";
    private String ELASTICITIES = "30, 20, 10";
    private String PICKING_STRATS = "SMART, NAIVE";
    private String OFFER_STRATS = "SMART, ABSTFT";
    private String CTOFFER_STRATS = "SMART, ABSTFT";
    private String PATIENCES = "100, 50";

    // SAJAS + REPAST
    @Override
    public String[] getInitParam() {
        return new String[] { "PRODUCT_NAME", "PRODUCT_PRICE", "NUM_SELLERS", "NUM_BUYERS", "SELLER_STOCK",
                "BUYER_STOCK", "SCAM_FACTORS", "ELASTICITIES", "PICKING_STRATS", "OFFER_STRATS", "CTOFFER_STRATS",
                "PATIENCES" };
    }

    @Override
    public String getName() {
        return "MAS 2nd Hand Marketplace";
    }

    public String getPRODUCT_NAME() {
        return PRODUCT_NAME;
    }

    public void setPRODUCT_NAME(String PRODUCT_NAME) {
        this.PRODUCT_NAME = PRODUCT_NAME;
    }

    public int getPRODUCT_PRICE() {
        return PRODUCT_PRICE;
    }

    public void setPRODUCT_PRICE(int PRODUCT_PRICE) {
        this.PRODUCT_PRICE = PRODUCT_PRICE;
    }

    public int getNUM_SELLERS() {
        return NUM_SELLERS;
    }

    public void setNUM_SELLERS(int NUM_SELLERS) {
        this.NUM_SELLERS = NUM_SELLERS;
    }

    public int getNUM_BUYERS() {
        return NUM_BUYERS;
    }

    public void setNUM_BUYERS(int NUM_BUYERS) {
        this.NUM_BUYERS = NUM_BUYERS;
    }

    public int getSELLER_STOCK() {
        return SELLER_STOCK;
    }

    public void setSELLER_STOCK(int SELLER_STOCK) {
        this.SELLER_STOCK = SELLER_STOCK;
    }

    public int getBUYER_STOCK() {
        return BUYER_STOCK;
    }

    public void setBUYER_STOCK(int BUYER_STOCK) {
        this.BUYER_STOCK = BUYER_STOCK;
    }

    public String getSCAM_FACTORS() {
        return SCAM_FACTORS;
    }

    public void setSCAM_FACTORS(String SCAM_FACTORS) {

        this.SCAM_FACTORS = SCAM_FACTORS;
    }

    public String getELASTICITIES() {
        return ELASTICITIES;
    }

    public void setELASTICITIES(String ELASTICITIES) {
        this.ELASTICITIES = ELASTICITIES;
    }

    public String getPATIENCES() {
        return PATIENCES;
    }

    public void setPATIENCES(String PATIENCES) {
        this.PATIENCES = PATIENCES;
    }

    public String getPICKING_STRATS() {
        return PICKING_STRATS;
    }

    public void setPICKING_STRATS(String PICKING_STRATS) {
        this.PICKING_STRATS = PICKING_STRATS;
    }

    public String getOFFER_STRATS() {
        return OFFER_STRATS;
    }

    public void setOFFER_STRATS(String OFFER_STRATS) {
        this.OFFER_STRATS = OFFER_STRATS;
    }

    public String getCTOFFER_STRATS() {
        return CTOFFER_STRATS;
    }

    public void setCTOFFER_STRATS(String CTOFFER_STRATS) {
        this.CTOFFER_STRATS = CTOFFER_STRATS;
    }
}