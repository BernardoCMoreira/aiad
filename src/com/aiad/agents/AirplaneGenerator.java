package com.aiad.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

public class AirplaneGenerator extends Agent {
    public int creationRate;
    public int airplaneCounter;
    public ContainerController controller;

    public AirplaneGenerator(int creationRate, ContainerController controller) {
        this.creationRate = creationRate;
        this.airplaneCounter = 0;
        this.controller = controller;
    }

    @Override
    protected void setup() {
        addBehaviour(new AirplaneCreator(this, this.creationRate));
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

        }
    }
}
