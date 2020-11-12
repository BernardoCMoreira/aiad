package com.aiad;
import com.aiad.agents.AirplaneGenerator;
import com.aiad.agents.ControlTower;
import com.aiad.agents.Runway;
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
            ac1 = mainContainer.acceptNewAgent("generator", new AirplaneGenerator(10000, mainContainer));
            ac1.start();
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

        AgentController ac6;
        try {
            ac6 = mainContainer.acceptNewAgent("runway2", new Runway());
            ac6.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac7;
        try {
            ac7 = mainContainer.acceptNewAgent("runway3", new Runway());
            ac7.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

}
