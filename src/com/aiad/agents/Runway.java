package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.RunwayOperationCfp;
import com.aiad.messages.RunwayOperationProposal;
import com.sun.source.tree.Tree;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import static com.aiad.Config.MAX_RUNWAY_CLEARANCE_TIME;
import static com.aiad.Config.TICKRATE;

public class Runway extends Agent {
    public static class Operation {
        private final int airplaneId;
        private int duration;

        public Operation(int airplaneId, int duration) {
            this.airplaneId = airplaneId;
            this.duration = duration;
        }

        public int getAirplaneId() {
            return airplaneId;
        }

        public int getDuration() {
            return duration;
        }

        public void tick() {
            duration--;
        }
    }

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
    final private TreeMap<Integer, Operation> operations = new TreeMap<>();

    private RunwayClearingBehaviour _clearingBehaviour;

    int id;
    boolean isClear;

    public Runway(){}

    int getEarliestSlot(int minTime) {
        var followingOperations = operations.tailMap(minTime);
        var time = minTime;
        for (var operation : followingOperations.entrySet()) {
            if (minTime + Config.OPERATION_LENGTH <= operation.getKey())
                break;
            else
                time = operation.getKey() + Config.OPERATION_LENGTH;
        }
        return time;
    }

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

    public boolean willBecomeObstructed() {
        return rand.nextDouble() < DEBRIS_APPEARANCE_PROBABILITY;
    }

    public void setObstructed() {
        var clearanceTime = rand.nextInt(MAX_RUNWAY_CLEARANCE_TIME);
        _clearingBehaviour = new RunwayClearingBehaviour(this, TICKRATE, clearanceTime);
        // TODO reschedule affected operations
    }

    public void setClear() {
        this._clearingBehaviour = null;
    }

    class ProposalBuilder extends ContractNetResponder {

        public ProposalBuilder(Agent agent, MessageTemplate mt) {
            super(agent, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException {
            var runway = (Runway) getAgent();
            // evaluate possibilities and make the best proposal possible
            try {
                var call = (RunwayOperationCfp) cfp.getContentObject();
                var slot = runway.getEarliestSlot(call.getMinTime());

                var proposal = RunwayOperationProposal.build(runway.getId(), slot);
                var reply = cfp.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(proposal);

                return reply;
            } catch (Exception e) {
                e.printStackTrace();
                throw new NotUnderstoodException(e.toString());
            }
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
