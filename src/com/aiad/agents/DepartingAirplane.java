package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.AID;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Random;

public class DepartingAirplane extends Airplane {

    private double DIRECTION =  new Random().nextInt(500  + 1);
    public DepartingAirplane(int id, int timeToArrive) {
        super(id, timeToArrive);
    }

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new TickerBehaviour(this, Config.PERIOD){
            @Override
            protected void onTick() {
                //updateNode();
                    if(getTotalTime() == 0){
                        System.out.println("Airplane: " + getId() + " DEPARTED !");
                        removeNode();
                        doDelete();
                    }
                }
        });
    }

    private void updateNode() {
        double x = node.getX();
        double y = node.getY();
        node.setX(x + ( (500 - x) / (getTotalTime())));
        node.setY(y + (DIRECTION - y) / (getTotalTime()));

    }
}
