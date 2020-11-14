package com.aiad.agents;

import com.aiad.Config;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class AirplaneGenerator extends Agent {
    public int creationRate;
    public int airplaneCounter;
    public ContainerController controller;

    protected FileWriter creationLog;

    public AirplaneGenerator(int creationRate, ContainerController controller) {
        this.creationRate = creationRate;
        this.airplaneCounter = 0;
        this.controller = controller;

        File file = new File(Config.AIRPLANE_CREATION_LOG);
        try {
            this.creationLog = new FileWriter(file);
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
            AgentController agentController;
            Agent airplane;

            int airplaneId = ++generator.airplaneCounter;
            int timeToArrive = new Random().nextInt(20);

            // randomly decides if the generated plane is arriving or departing
            if (new Random().nextBoolean()) {   // arriving
                int fuelRemaining = timeToArrive + new Random().nextInt(5);
                airplane = new ArrivingAirplane(airplaneId, timeToArrive, fuelRemaining);
                System.out.println("GENERATOR :: ARRIVING : airplane" + airplaneId + " ETA : " + timeToArrive + " FUEL : " + fuelRemaining);
            } else {    // departing
                airplane = new DepartingAirplane(airplaneId, timeToArrive);
                System.out.println("GENERATOR :: DEPARTING : airplane" + airplaneId + " ETA : " + timeToArrive);
            }

            try {
                agentController = controller.acceptNewAgent("airplane" + airplaneId, airplane);
                agentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

            logAirplaneCreation((Airplane) airplane);
        }

    }
}
