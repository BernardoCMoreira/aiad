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

import javax.swing.*;
import java.awt.*;

public class JADELauncher {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();

        JFrame frame = new JFrame("Airport");
        frame.setSize(1300,800);
        frame.setLayout(new GridLayout(3, 1));

        //p1.setParameter(...);
        ContainerController mainContainer = rt.createMainContainer(p1);

        // create an AirplaneGenerator agent
        AgentController ac1;
        try {
            ac1 = mainContainer.acceptNewAgent("generator", new AirplaneGenerator(Config.CREATION_RATE, mainContainer));
            ac1.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        // create the ControlTower agent
        AgentController ac4;
        try {
            ac4 = mainContainer.acceptNewAgent("tower", new ControlTower());
            ac4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac5;
        try {
            ac5 = mainContainer.acceptNewAgent("runway1", new Runway(1, frame));
            ac5.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac6;
        try {
            ac6 = mainContainer.acceptNewAgent("runway2", new Runway(2, frame));
            ac6.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac7;
        try {
            ac7 = mainContainer.acceptNewAgent("runway3", new Runway(3, frame));
            ac7.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
/*        AgentController ac8;
        try {
            ac8 = mainContainer.acceptNewAgent("runway4", new Runway(4, frame));
            ac8.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac9;
        try {
            ac9 = mainContainer.acceptNewAgent("runway5", new Runway(5, frame));
            ac9.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac10;
        try {
            ac10 = mainContainer.acceptNewAgent("runway6", new Runway(6, frame));
            ac10.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac11;
        try {
            ac11 = mainContainer.acceptNewAgent("runway7", new Runway(7, frame));
            ac11.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentController ac12;
        try {
            ac12 = mainContainer.acceptNewAgent("runway8", new Runway(8, frame));
            ac12.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }*/

    }


}
