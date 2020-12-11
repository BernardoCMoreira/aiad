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
import uchicago.src.sim.gui.*;
import uchicago.src.sim.network.DefaultDrawableNode;

import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.network.DefaultDrawableNode;

import javax.naming.ldap.Control;
import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;


public class RepastLauncher extends Repast3Launcher {

    private ContainerController mainContainer;
    JFrame frame = new JFrame("Airport");
    // private Histogram histogram;
    public static Plot scatterPlot;
    public static OpenSequenceGraph open;
    public static OpenSequenceGraph openRunway;
    public ControlTower controlTower;
    public static int[] runwaysList;
    public static ArrayList<Runway> runwaysArrayList = new ArrayList<>();
    public static List<DefaultDrawableNode> nodes;


    @Override
    public String[] getInitParam() {
        return new String[]{
                "CREATION_RATE",
                "NUM_RUNWAYS",
                "OBSTRUCTION_PROB"
        };
    }

    public int CREATION_RATE = Config.CREATION_RATE;
    public int NUM_RUNWAYS = Config.NUM_RUNWAYS;
    public double OBSTRUCTION_PROB = Config.DEBRIS_APPEARANCE_PROBABILITY;

    public int getCREATION_RATE() {
        return CREATION_RATE;
    }

    public void setCREATION_RATE(int CREATION_RATE) {
        this.CREATION_RATE = CREATION_RATE;
    }

    public void setNUM_RUNWAYS(int NUM_RUNWAYS) {
        this.NUM_RUNWAYS = NUM_RUNWAYS;
    }

    public int getNUM_RUNWAYS() {
        return NUM_RUNWAYS;
    }

    public void setOBSTRUCTION_PROB(double OBSTRUCTION_PROB) {
        this.OBSTRUCTION_PROB = OBSTRUCTION_PROB;
    }

    public double getOBSTRUCTION_PROB() {
        return OBSTRUCTION_PROB;
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
        runwaysList = new int[NUM_RUNWAYS];
    }

    private void launchAgents() {
        nodes = new ArrayList<>();

        frame.setSize(1300,800);
        frame.setLayout(new GridLayout(3, 1));

        // create an AirplaneGenerator agent
        AgentController ac1;
        try {
            ac1 = mainContainer.acceptNewAgent("generator", new AirplaneGenerator(CREATION_RATE, mainContainer, this));
            ac1.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        // create the ControlTower agent
        try {
            AgentController ac4;
            controlTower = new ControlTower();
            ac4 = mainContainer.acceptNewAgent("tower", controlTower);
            ac4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        nodes.add(generateNode("control tower", Color.blue, WIDTH/2, HEIGHT/2));

        // create Runway agents
        for (int i = 1; i <= NUM_RUNWAYS; i++) {
            AgentController ac5;
            try {
                ac5 = mainContainer.acceptNewAgent("runway" + i, new Runway(i, frame));
                runwaysArrayList.add(new Runway(i,frame));
                ac5.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
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

    private MyDisplaySurface graphSurface;
    private final int WIDTH = 500;
    private final int HEIGHT = 500;
    private Network2DDisplay network;


    public void buildDisplay() {
        scatterPlot = new Plot("graph", this);
        scatterPlot.setXRange(0,50);
        scatterPlot.setYRange(0,50);
        scatterPlot.setAxisTitles("Current Operations (units)", "Wait Time (ticks)");
        scatterPlot.setConnected(false);
        scatterPlot.display();
        getSchedule().scheduleActionAtInterval(1, scatterPlot, "updateGraph", Schedule.LAST);

        open = new OpenSequenceGraph("Service", this);
        open.setAxisTitles("time", "Totals");
        open.addSequence("Total_arrivals", new Sequence (){
            public double getSValue(){
                return controlTower.getTotalArrivals();
            }
        });
        open.addSequence("Total_departures", new Sequence (){
            public double getSValue(){
                return controlTower.getTotalDepartures();
            }
        });
        open.addSequence("Total_redirects", new Sequence (){
            public double getSValue(){
                return controlTower.getTotalRedirect();
            }
        });
        open.display();
        getSchedule().scheduleActionAtInterval(10, open, "step", Schedule.LAST);

        if (graphSurface != null) {
            graphSurface.dispose();
        }

        graphSurface = new MyDisplaySurface(this, "test display surface");
        registerDisplaySurface("test", graphSurface);
        network = new Network2DDisplay(nodes, WIDTH, HEIGHT);
        graphSurface.addDisplayableProbeable(network, "airport graph");
        graphSurface.addZoomable(network);
        addSimEventListener(graphSurface);
        graphSurface.display();


        getSchedule().scheduleActionAtInterval(100, graphSurface, "updateDisplay", Schedule.LAST);

        runwaysSequenceGraph();

    }

    public void runwaysSequenceGraph(){
        openRunway = new OpenSequenceGraph("Service", this);
        openRunway.setAxisTitles("runway_id", "Total operations");
        for (Runway runway : runwaysArrayList) {
            openRunway.addSequence("Runway ID" + runway.getId(), new Sequence() {
                public double getSValue() {
                    return runwaysList[runway.getId()-1];
                }
            });
        }

        openRunway.display();
        getSchedule().scheduleActionAtInterval(10, openRunway, "step", Schedule.LAST);
    }
    public void updateNetworkDisplay() {
        int operationsCount = 0;
        for(int i=0; i<RepastLauncher.runwaysList.length; i++){
            operationsCount += RepastLauncher.runwaysList[i];
        }
        ControlTower.operationsInProcess = operationsCount;

        if(network != null) {
            graphSurface.removeProbeableDisplayable(network);
        }

        network = new Network2DDisplay(nodes, WIDTH, HEIGHT);
        graphSurface.addDisplayableProbeable(network, "airport graph" + network.hashCode());
        graphSurface.addZoomable(network);
        addSimEventListener(graphSurface);

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
