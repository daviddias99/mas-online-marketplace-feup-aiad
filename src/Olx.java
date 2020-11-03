import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Seller> sellers = new ArrayList<>();
    private List<Buyer> buyers = new ArrayList<>();

    public Olx(boolean mainMode) {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();

        if (mainMode)
            this.container = rt.createMainContainer(p);
        else
            this.container = rt.createAgentContainer(p);

        createSellers();
        createBuyers();
    }

    public void createSellers() {

        Product[] products = new Product[1];
        products[0] = new Product("pc", 15);
        for (int i = 0; i < 3; i++) {
            // if(i == 1)
            // products.put("skate",20);
            // else if(i==2)
            // products.remove("pc");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            Seller newSeller = new Seller(products, i * 33 + 34);
            try {
                AgentController ac = this.container.acceptNewAgent("seller_" + i, newSeller);
                ac.start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }
            this.sellers.add(newSeller);
        }
    }

    public void createBuyers(){
        String[] products = new String[]{ "pc" };
        Buyer newBuyer2 = new Buyer(products);

        try {
            // this.container.acceptNewAgent("buyer_0", newBuyer).start();
            this.container.acceptNewAgent("buyer_1", newBuyer2).start();
        } catch (StaleProxyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // this.buyers.add(newBuyer);
        this.buyers.add(newBuyer2);
    }

    public static void main(String[] args) throws IOException {
        // TODO: por a aceitar de args as variáveis independentes e passar para o Olx
        // TODO: controlar args
        // Olx olx = new Olx(Boolean.parseBoolean(args[0]));

        // try {
        //     olx.container.kill();
        //     olx.rt.shutDown();
        // } catch (StaleProxyException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        Config config = Config.read("config.yaml");
        for (Product p : config.getProducts()) {
            System.out.println(p.toString());
        }
        for (Buyer b : config.getBuyers()) {
            System.out.println(b.toString());
        }
        for (Seller s : config.getSellers()) {
            System.out.println(s.toString());
        }
    }

}
