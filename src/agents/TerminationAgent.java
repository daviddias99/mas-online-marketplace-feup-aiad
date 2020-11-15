package agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import utils.TerminationListener;

public class TerminationAgent extends Agent {
    private TerminationListener terminationListener;

    public void setup() {
        addBehaviour(new TerminationBehaviour());

        System.out.println("Terminator agent started.");
    }

    public void takeDown() {
        this.terminationListener.terminated(this);
    }

    public void setTerminationListener(TerminationListener listener) {
        this.terminationListener = listener;
    }

    class TerminationBehaviour extends Behaviour {
        boolean done = false;

        public void action() {
            Codec codec = new SLCodec();
            Ontology jmo = JADEManagementOntology.getInstance();
            getContentManager().registerLanguage(codec);
            getContentManager().registerOntology(jmo);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAMS());
            msg.setLanguage(codec.getName());
            msg.setOntology(jmo.getName());
            try {
                getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
                send(msg);
            } catch (Exception e) {
                System.out.println("Terminator Agent: Could not send shutdown request to AMS");
            }

            done = true;
        }

        public boolean done() {
            return done;
        }
    }
}