package olx.draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import olx.agents.Buyer;
import olx.agents.Seller;
import olx.agents.strategies.counter_offer.CounterOfferStrategy;
import olx.utils.Util;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.engine.ScheduleBase;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

public class OlxNetwork {
    private MyDisplaySurface dsurf;
    private Repast3Launcher launcher;
    private Network2DDisplay network;
    private static List<DefaultDrawableNode> nodes = new ArrayList<>();
    private int nBuyers;
    private int nSellers;
    private int WIDTH = 1920, HEIGHT = 1080;
    private Map<CounterOfferStrategy.Type, Integer> buyerStrategies;

    public OlxNetwork(Repast3Launcher launcher, List<Buyer> buyers, List<Seller> sellers, Map<CounterOfferStrategy.Type, Integer> buyerStrategies) {
        this.launcher = launcher;
        this.nBuyers = 0;
        this.nSellers = 0;

        this.buyerStrategies = buyerStrategies;

        // display surface
        if (this.dsurf != null)
            this.dsurf.dispose();
        this.dsurf = new MyDisplaySurface(this.launcher, "MAS 2nd Hand Marketplace Display");
        this.launcher.registerDisplaySurface("MAS 2nd Hand Marketplace Display", this.dsurf);

        // Initate Sellers
        this.addBuyers(buyers);
        this.addSellers(sellers);

        this.dsurf.display();
        this.launcher.getSchedule().scheduleActionAtInterval(1, this.dsurf, "updateDisplay", ScheduleBase.LAST);
    }

    public void addBuyers(List<Buyer> buyers) {
        for(int i = 0; i < buyers.size(); i++, this.nBuyers++){
            Buyer buyer = buyers.get(i);

            int index = this.buyerStrategies.get(buyer.getCounterOfferStrategy().getType());
            int delta = (HEIGHT - WIDTH / 115) / this.buyerStrategies.size();
            int yMin = index * delta;
            int yMax = yMin + delta;
            Color buyerColor = Util.getBuyerColor(buyer.getCounterOfferStrategy().getType());

            DefaultDrawableNode node = generateNode(Util.localNameToLabel("buyer_" + this.nBuyers), buyerColor,
                Util.randomBetween(0, WIDTH / 3), Util.randomBetween(yMin, yMax));
            buyer.setNode(node);
        }
        this.updateNetwork();
    }

    public void addSellers(List<Seller> sellers){
        for(int i = 0; i < sellers.size(); i++, this.nSellers++){
            DefaultDrawableNode node = generateNode(Util.localNameToLabel("seller_" + this.nSellers),
                Util.getSellerColor(sellers.get(i).getCredibility()), Util.randomBetween(2 * WIDTH / 3, WIDTH - WIDTH / 115),
                Util.randomBetween(0, HEIGHT - WIDTH / 115));
            sellers.get(i).setNode(node);
        }
        this.updateNetwork();
    }

    public void updateNetwork() {
        if (this.network != null)
            this.dsurf.removeProbeableDisplayable(this.network);

        this.network = new Network2DDisplay(nodes, WIDTH, HEIGHT);
        this.dsurf.addDisplayableProbeable(this.network, "Network Display" + this.network.hashCode());
        this.dsurf.addZoomable(this.network);
        this.launcher.addSimEventListener(this.dsurf);
    }

    public DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        OvalNetworkItem oval = new OvalNetworkItem(x, y);
        oval.allowResizing(false);
        oval.setHeight(WIDTH / 115);
        oval.setWidth(WIDTH / 115);

        DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
        node.setColor(color);
        node.setBorderColor(color);
        node.setBorderWidth(1);

        nodes.add(node);
        return node;
    }

    public static DefaultDrawableNode getNode(String label) {
        for (DefaultDrawableNode node : nodes) {
            if (node.getNodeLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }

    public void close() {
        this.dsurf.dispose();
        OlxNetwork.nodes = new ArrayList<>();
    }

    public void removeNode(DefaultDrawableNode node) {
        nodes.remove(node);
        this.updateNetwork();
    }
}
