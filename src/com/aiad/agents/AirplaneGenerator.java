package com.aiad.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

public class AirplaneGenerator extends Agent {
    public int tickForNewAirplanes;
    public int airplaneCounter;
    public ContainerController controller;

    public AirplaneGenerator(int creationRate, ContainerController controller) {
        this.tickForNewAirplanes = creationRate;
        this.airplaneCounter = 0;
        this.controller = controller;
    }

    @Override
    protected void setup() {
        addBehaviour(new TickerBehaviour(this, this.tickForNewAirplanes) {
            AirplaneGenerator plane = (AirplaneGenerator) myAgent;

            @Override
            protected void onTick() {
                AgentController ac;
                try {
                    plane.airplaneCounter++;
                    String message = airplaneMessageGenerator(plane.airplaneCounter);
                    ac = plane.controller.acceptNewAgent("airplane" + plane.airplaneCounter, new ArrivingAirplane(message));
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public String airplaneMessageGenerator(int id){
        String message;
        int landedInt = new Random().nextInt(2);
        int waitTime = 0;                   // initially is 0, but control tower might change it
        boolean landed = landedInt == 0;    // if == 0 landed is true, else is false
        int timeToArrive, fuelRemaining;
        if (landed) {
            timeToArrive = 0;
            fuelRemaining = 100;
        }else{
            timeToArrive = new Random().nextInt(20);
            fuelRemaining = timeToArrive + new Random().nextInt(5);
        }
        message = id + " " + waitTime + " " + timeToArrive + " " + fuelRemaining + " " + landed;
        return message;
    }
}
