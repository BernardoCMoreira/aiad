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
        startJFrame();
        Profile p1 = new ProfileImpl();
        //p1.setParameter(...);
        ContainerController mainContainer = rt.createMainContainer(p1);

        // create an AirplaneGenerator agent
        AgentController ac1;
        try {
            ac1 = mainContainer.acceptNewAgent("generator", new AirplaneGenerator(1000, mainContainer));
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
            ac5 = mainContainer.acceptNewAgent("runway1", new Runway(1));
            ac5.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac6;
        try {
            ac6 = mainContainer.acceptNewAgent("runway2", new Runway(2));
            ac6.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac7;
        try {
            ac7 = mainContainer.acceptNewAgent("runway3", new Runway(3));
            ac7.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public static void startJFrame(){

        JFrame frame = new JFrame("Airport");
        frame.setSize(600,700);
        JPanel panel1 = new JPanel();
        panel1.setBackground(Color.GRAY);
        panel1.setLayout(new FlowLayout());

        JTable table = new JTable(1,20);
        table.setBounds(10,170, 600, 40);
        table.setRowHeight(40);
        JLabel l1 = new JLabel("runway 1");
        l1.setBounds(10,10,800,40);

        panel1.add(l1);
        panel1.add(table);
        frame.add(panel1);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.show();
    }

}
