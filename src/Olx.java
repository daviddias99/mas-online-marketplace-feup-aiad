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

        Map<String, Float> products = new HashMap<>();
        products.put("pc", 150.0f);
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
        List<String> products = new ArrayList<>();
        products.add("pc");
        // Buyer newBuyer = new Buyer(products);
        // products.add("skate");
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

    public static void main(String[] args) {
        // TODO: por a aceitar de args as variáveis independentes e passar para o Olx
        // TODO: controlar args
        Olx olx = new Olx(Boolean.parseBoolean(args[0]));

        // try {
        //     olx.container.kill();
        //     olx.rt.shutDown();
        // } catch (StaleProxyException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

    }

}
