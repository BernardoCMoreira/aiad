package com.aiad.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

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

    class ProposalBuilder extends ContractNetResponder {

        public ProposalBuilder(Agent agent, MessageTemplate mt) {
            super(agent, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {

            // evaluate possibilities and make the best proposal possible

            ACLMessage reply = cfp.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            // set the content of the message
            reply.setContent("this is an example proposal");

            return reply;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println(myAgent.getLocalName() + " got an accept!");

            // realize the activity that was proposed

            // create a reply informing if the activity was completed successfully
            ACLMessage result = accept.createReply();
            result.setPerformative(ACLMessage.INFORM);
            result.setContent("this is the result");

            return result;
        }
    }
}
