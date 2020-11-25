package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import models.Product;
import models.SellerOfferInfo;
import agents.Seller;
import jade.core.AID;
import sajas.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import sajas.proto.AchieveREInitiator;

public class AskPriceSeller extends AchieveREInitiator {

    private Product product;
    private Seller seller;

    /**
     * Seller <agent> is asking the selling price of <product>
     */
    public AskPriceSeller(Product product, Seller agent, ACLMessage msg) {
        super(agent, msg);
        this.product = product;
        this.seller = agent;
    }

    @Override
    public Seller getAgent() {
        return (Seller) super.getAgent();
    }

    protected Product getProduct() {
        return this.product;
    }

    @Override
    protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
        Vector<ACLMessage> v = new Vector<>();

        // Query df service for agents who are selling <product>
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(this.product.getName());
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);
            // No agents are selling <product>
            if (result.length == 0) {
                this.handleNoResults();
                return v;
            }

            // Add each one as receiver for price asking
            for (int i = 0; i < result.length; ++i)
                msg.addReceiver(result[i].getName());

            // The <product> is sent as the content so that the
            // seller knows to which product the request pertains to
            msg.setContentObject(this.product);
            
        } catch (FIPAException | IOException fe) {
            this.seller.logger().warning("/!\\ %s encounterd an error searching for sellers.%n");
            this.handleNoResults();
            return v;
        }
        
        v.add(msg);
        // Logging
        this.logSellerList(msg);
        System.out.printf("%s asked prices for product %s %n", this.getAgent().getLocalName(), this.product);
        return v;
    }

    protected void handleNoResults() {

        // No other sellers are currenttly selling <product>
        // set selling price accordingly
        Seller s = this.getAgent();
        Product p = this.getProduct();
        s.addProduct(p, this.seller.getPricePickingStrategy().calculateInitialPrice(s, p));
        s.logger().info(String.format("! %s found no sellers for product %s. Setting price at %.2f", s.getLocalName(),
                p.getName(), s.getProductPrice(this.getProduct().getName())));
        // Register that agent is selling <product> in the DF registry
        s.register(p);
    }

    @Override
    protected void handleAllResultNotifications(Vector resultNotifications) {
        Product p = this.getProduct();
        List<SellerOfferInfo> marketPrices = this.collectPrices(resultNotifications);
        this.logPriceString(marketPrices, p);

        // Other sellers are currenttly selling <product>
        // set selling price accordingly
        this.seller.addProduct(p, this.seller.getPricePickingStrategy().calculateInitialPrice(seller, p, marketPrices));
        this.seller.logger().info(String.format("! %s set product %s price at %.2f", seller.getLocalName(), p.getName(),
                seller.getProductPrice(p.getName())));
        this.seller.register(p);
        System.out.printf("%s received prices for product %s %n", this.getAgent().getLocalName(), this.product);
    }

    // HELPERS

    private List<SellerOfferInfo> collectPrices(Vector resultNotifications) {

        List<SellerOfferInfo> marketPrices = new ArrayList<>();
        // Collect current market prices
        for (int i = 0; i < resultNotifications.size(); i++) {
            ACLMessage message = (ACLMessage) resultNotifications.get(i);
            try {
                marketPrices.add((SellerOfferInfo) message.getContentObject());
            } catch (UnreadableException e) {
                this.getAgent().logger().info(String.format("/!\\ %s could not read object sent by %s",
                        this.getAgent().getLocalName(), message.getSender().getLocalName()));
            }
        }

        return marketPrices;
    }

    private void logSellerList(ACLMessage msg) {
        // Logging
        StringBuilder sb = new StringBuilder(String.format("< %s asked the price of %s to the following sellers: [",
                this.getAgent().getLocalName(), this.getProduct().getName()));
        Iterator<AID> it = msg.getAllReceiver();
        boolean first = true;
        while (it.hasNext())
            if (first) {
                sb.append(String.format("%s", it.next().getLocalName()));
                first = false;
            } else
                sb.append(String.format(", %s", it.next().getLocalName()));
        this.getAgent().logger().info(sb.append("]").toString());

    }

    private void logPriceString(List<SellerOfferInfo> marketPrices, Product p) {
        boolean first = true;

        StringBuilder sb = new StringBuilder(
                String.format("> %s found that product %s has %d sellers with these prices: [", seller.getLocalName(),
                        p.getName(), marketPrices.size()));
        for (SellerOfferInfo soInfo : marketPrices)
            if (first) {
                sb.append(String.format("%.2f - %d", soInfo.getOfferedPrice(), soInfo.getSellerCredibility()));
                first = false;
            } else
                sb.append(String.format(", %.2f - %d", soInfo.getOfferedPrice(), soInfo.getSellerCredibility()));
        this.seller.logger().info(sb.append("]").toString());

    }
}
