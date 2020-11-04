package com.aiad.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Random;

import static com.aiad.Config.MAX_RUNWAY_CLEARANCE_TIME;
import static com.aiad.Config.TICKRATE;

public class Runway extends Agent {

    public static class RunwayClearingBehaviour extends TickerBehaviour {
        final private int clearanceTime;

        public RunwayClearingBehaviour(Runway runway, int tickRate, int clearanceTime) {
            super(runway, 1000 / tickRate);
            this.clearanceTime = clearanceTime;
        }

        @Override
        protected void onTick() {
            if (getTickCount() == clearanceTime) {
                ((Runway) this.getAgent()).setClear();
            }
            this.stop();
        }
    }

    final public static double DEBRIS_APPEARANCE_PROBABILITY = 0.1d;
    final private Random rand = new Random();
    private RunwayClearingBehaviour _clearingBehaviour;

    int id;
    boolean isClear;

    public Runway(){}

    /*
     *   The message will be the following :
     *   " id isClear"
     *
     */

    public Runway(String message){
        String[] splitMessage = message.split(" ");
        this.id = Integer.parseInt(splitMessage[0]);
        this.isClear = splitMessage[1].equals("true");
    }

    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
        service.setName("runway");
        service.setType("runway");

        description.addServices(service);

        try {
            DFService.register(this, description);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public int getId(){
        return this.id;
    }

    public boolean isClear(){
        return this.isClear;
    }

    public void processPlaneOperation() {
        if (rand.nextDouble() < DEBRIS_APPEARANCE_PROBABILITY) {
            setObstructed();
        }
    }

    public void setObstructed() {
        var clearanceTime = rand.nextInt(MAX_RUNWAY_CLEARANCE_TIME);
        _clearingBehaviour = new RunwayClearingBehaviour(this, TICKRATE, clearanceTime);
    }

    public void setClear() {
        this._clearingBehaviour = null;
    }
}
