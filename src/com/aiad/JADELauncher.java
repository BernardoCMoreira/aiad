package com.aiad;
import com.aiad.agents.Airplane;
import com.aiad.agents.ControlTower;
import com.aiad.agents.Runway;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JADELauncher {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        Profile p1 = new ProfileImpl();
        //p1.setParameter(...);
        ContainerController mainContainer = rt.createMainContainer(p1);

        AgentController ac1;
        try {
            ac1 = mainContainer.acceptNewAgent("airplane1", new Airplane());
            ac1.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac3;
        try {
            ac3 = mainContainer.acceptNewAgent("airplane2", new Airplane());
            ac3.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac4;
        try {
            ac4 = mainContainer.acceptNewAgent("tower", new ControlTower());
            ac4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac5;
        try {
            ac5 = mainContainer.acceptNewAgent("runway1", new Runway());
            ac5.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

}
