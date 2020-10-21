package src;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Olx {
    private Runtime rt;
    private Profile p;
    private ContainerController mainContainer;

    public Olx() {
        this.rt = Runtime.instance();
        this.p = new ProfileImpl();
        this.mainContainer = rt.createMainContainer(p);
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
