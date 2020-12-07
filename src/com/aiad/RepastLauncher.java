package com.aiad;

import com.aiad.agents.AirplaneGenerator;
import com.aiad.agents.ControlTower;
import com.aiad.agents.Runway;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;

import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import sajas.wrapper.AgentController;

import uchicago.src.sim.analysis.Plot;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.network.DefaultDrawableNode;

import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;


public class RepastLauncher extends Repast3Launcher {

    private ContainerController mainContainer;
    JFrame frame = new JFrame("Airport");


    // private Histogram histogram;
    public static Plot scatterPlot;

    public static List<DefaultDrawableNode> nodes;


    @Override
    public String[] getInitParam() {
        return new String[0];
    }

    @Override
    public String getName() {
        return "SAJaS Project";
    }

    @Override
    protected void launchJADE() {

        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        mainContainer = rt.createMainContainer(p1);

        launchAgents();
    }

    private void launchAgents() {
        nodes = new ArrayList<>();

        frame.setSize(1300,800);
        frame.setLayout(new GridLayout(3, 1));

        // create an AirplaneGenerator agent
        AgentController ac1;
        try {
            ac1 = mainContainer.acceptNewAgent("generator", new AirplaneGenerator(Config.CREATION_RATE, mainContainer, nodes));
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
        nodes.add(generateNode("control tower", Color.blue, WIDTH/2, HEIGHT/2));

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


    }

    private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
        OvalNetworkItem oval = new OvalNetworkItem(x,y);
        oval.allowResizing(false);
        oval.setHeight(5);
        oval.setWidth(5);

        DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
        node.setColor(color);

        return node;
    }

    @Override
    public void setup() {
        super.setup();

        // property descriptors
        // ...
    }

    // this is called when the play button is pressed in the gui
    @Override
    public void begin() {
        super.begin();

        buildModel();

        // display surfaces, spaces, displays, plots, ...
        // ...
        buildDisplay();

    }

    public void buildModel() {

    }

    private DisplaySurface graphSurface;
    private final int WIDTH = 500;
    private final int HEIGHT = 500;


    public void buildDisplay() {
        scatterPlot = new Plot("graph", this);
        scatterPlot.setAxisTitles("Current Operations (units)", "Wait Time (ticks");
        scatterPlot.display();
        scatterPlot.setConnected(false);


        if (graphSurface != null) {
            graphSurface.dispose();
        }

        graphSurface = new DisplaySurface(this, "test display surface");
        registerDisplaySurface("test", graphSurface);
        Network2DDisplay graphDisplay = new Network2DDisplay(nodes, WIDTH, HEIGHT);
        graphSurface.addDisplayableProbeable(graphDisplay, "airport graph");
        graphSurface.addZoomable(graphDisplay);
        addSimEventListener(graphSurface);
        graphSurface.display();

        getSchedule().scheduleActionAtInterval(0.5, graphSurface, "updateDisplay", Schedule.LAST);

    }
    /**
     * Launching Repast3
     * @param args
     */
    public static void main(String[] args) {
        boolean BATCH_MODE = false;
        SimInit init = new SimInit();
        init.setNumRuns(1);   // works only in batch mode
        init.loadModel(new RepastLauncher(), null, BATCH_MODE);
    }

}
