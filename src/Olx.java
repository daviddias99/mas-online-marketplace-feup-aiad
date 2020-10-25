package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import src.agents.Buyer;
import src.agents.Seller;

public class Olx {
    private Runtime rt;
    private Profile p;
    private ContainerController mainContainer;
    private List<Seller> sellers = new ArrayList<>();
    private List<Buyer> buyers = new ArrayList<>();
    


    public Olx() {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();
        this.mainContainer = rt.createMainContainer(p);

        createSellers();
        createBuyers();
    }

    public void createSellers(){
        Map<String, Integer> products = new HashMap<>();
        products.put("pc", 150);
        for(int i = 0; i < 3; i++)
            this.sellers.add(new Seller(products, i * 33 + 34));
    }

    public void createBuyers(){
        List<String> products = new ArrayList<>();
        products.add("pc");
        this.buyers.add(new Buyer(products));
    }

    public static void main(String[] args) {
        // TODO: por a aceitar de args as variáveis independentes e passar para o Olx
        Olx olx = new Olx();

        try {
            olx.mainContainer.kill();
            olx.rt.shutDown();
        } catch (StaleProxyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}
