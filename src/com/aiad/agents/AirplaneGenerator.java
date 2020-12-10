package com.aiad.agents;

import com.aiad.Config;
import com.aiad.RepastLauncher;
import com.bbn.openmap.tools.symbology.milStd2525.CodeFunctionID;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import sajas.wrapper.AgentController;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import uchicago.src.sim.gui.Drawable2DNode;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableEdge;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AirplaneGenerator extends Agent {
    public int creationRate;
    public int airplaneCounter;
    public ContainerController controller;
    public static int tickerCounter = 0;
    protected FileWriter creationLog;

    private RepastLauncher launcher;

    public AirplaneGenerator(int creationRate, ContainerController controller, RepastLauncher launcher) {
        this.creationRate = creationRate;
        this.airplaneCounter = 0;
        this.controller = controller;
        this.launcher = launcher;

        File file = new File(Config.AIRPLANE_CREATION_LOG);
        try {
            this.creationLog = new FileWriter(file);
            creationLog.write("id, type, time_to_arrive, fuel_remaining\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setup() {
        addBehaviour(new AirplaneCreator(this, this.creationRate));
    }

    protected void logAirplaneCreation(Airplane airplane) {
        try {
            if (airplane instanceof ArrivingAirplane) {
                ArrivingAirplane arrivingAirplane = (ArrivingAirplane) airplane;
                this.creationLog.append(arrivingAirplane.id + ", arriving, " + arrivingAirplane.timeToArrive + ", " + arrivingAirplane.fuelRemaining + "\n");
            } else {
                DepartingAirplane departingAirplane = (DepartingAirplane) airplane;
                this.creationLog.append(departingAirplane.id + ", departing, " + departingAirplane.timeToArrive + ", " + "NA\n");
            }
            this.creationLog.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        try {
            this.creationLog.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class AirplaneCreator extends TickerBehaviour {

        private AirplaneGenerator generator;
        private ContainerController controller;

        public AirplaneCreator(Agent a, int creationRate) {
            super(a, creationRate);

            this.generator = (AirplaneGenerator) a;
            this.controller = generator.controller;
        }

        @Override
        protected void onTick() {
            tickerCounter++;
            AgentController agentController;
            Agent airplane;

            int airplaneId = ++generator.airplaneCounter;
            int timeToArrive = new Random().nextInt((Config.MAX_TIME_TO_ARRIVE - Config.MIN_TIME_TO_ARRIVE) + 1) + Config.MIN_TIME_TO_ARRIVE;

            // randomly decides if the generated plane is arriving or departing
            if (new Random().nextBoolean()) {   // arriving
                int fuelRemaining = timeToArrive + new Random().nextInt(Config.MAX_FUEL_REMAINING);
                airplane = new ArrivingAirplane(airplaneId, timeToArrive, fuelRemaining);
                System.out.println("GENERATOR :: ARRIVING : airplane" + airplaneId + " ETA : " + timeToArrive + " FUEL : " + fuelRemaining);

                DefaultDrawableNode node = generateNode(airplaneId + "", Color.yellow, 10, new Random().nextInt(500));
                ((Airplane)airplane).setNode(launcher, node);
            } else {    // departing
                airplane = new DepartingAirplane(airplaneId, timeToArrive);
                System.out.println("GENERATOR :: DEPARTING : airplane" + airplaneId + " ETA : " + timeToArrive);

                DefaultDrawableNode node = generateNode(airplaneId + "", Color.green, 480, new Random().nextInt(500));
                ((Airplane)airplane).setNode(launcher, node);
            }
            launcher.updateNetworkDisplay();


            try {
                agentController = controller.acceptNewAgent("airplane" + airplaneId, airplane);
                agentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

            logAirplaneCreation((Airplane) airplane);
        }

        private DefaultDrawableNode generateNode(String label, Color color, int x, int y) {
            RectNetworkItem oval = new RectNetworkItem(x,y);
            oval.allowResizing(false);
            oval.setHeight(5);
            oval.setWidth(10);

            DefaultDrawableNode node = new DefaultDrawableNode(label, oval);
            node.setColor(color);

            DefaultDrawableEdge edge = new DefaultDrawableEdge(node, launcher.nodes.get(0));
            node.addOutEdge(edge);

            return node;
        }
    }
}
