package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.RunwayOperationCfp;
import com.aiad.messages.RunwayOperationProposal;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import static com.aiad.Config.MAX_RUNWAY_CLEARANCE_TIME;
import static com.aiad.Config.TICKRATE;

public class Runway extends Agent {
    final public static double DEBRIS_APPEARANCE_PROBABILITY = 0.1d;
    final private Random rand = new Random();
    final private TreeMap<Integer, Operation> operations = new TreeMap<>();
    int id;
    boolean isClear = true;
    private RunwayClearingBehaviour _clearingBehaviour;
    private RunwayUpdatingBehaviour _updatingBehaviour;

    public Runway(int id) {
        this.id = id;
    }

    public Runway(String message) {
        String[] splitMessage = message.split(" ");
        this.id = Integer.parseInt(splitMessage[0]);
        this.isClear = splitMessage[1].equals("true");
    }

    int getEarliestSlot(int minTime) {
        var followingOperations = operations.tailMap(minTime);
        var time = isClear ? minTime : Math.max(minTime, _clearingBehaviour.clearanceTime);
        for (var operation : followingOperations.entrySet()) {
            if (minTime + Config.OPERATION_LENGTH <= operation.getKey())
                break;
            else
                time = operation.getKey() + Config.OPERATION_LENGTH;
        }
        return time;
    }

    void updateOperations() {
        // deal with 0 value
        var willBeObstructed = false;
        if (operations.containsKey(0)) {
            operations.get(0).tick();
            if (operations.get(0).getDuration() == 0) {
                operations.remove(0);
                willBeObstructed = willBecomeObstructed();
            }
        }
        // decrement time to next operations
        var keys = new ArrayList<>(operations.tailMap(0, false).keySet());
        for (var key : keys) {
            var operation = operations.get(key);
            operations.remove(key);
            operations.put(key - 1, operation);
        }

        if (willBeObstructed)
            setObstructed();
    }


    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
        service.setName("runway");
        service.setType("runway");

        setupUpdatingBehaviour();

        addBehaviour(new ProposalBuilder(this, new MessageTemplate(
                (MessageTemplate.MatchExpression) msg -> msg.getPerformative() == ACLMessage.CFP)
        ));

        description.addServices(service);

        try {
            DFService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /*
     *   The message will be the following :
     *   " id isClear"
     *
     */

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    public boolean isClear() {
        return this.isClear;
    }

    public boolean willBecomeObstructed() {
        return rand.nextDouble() < DEBRIS_APPEARANCE_PROBABILITY;
    }

    public void setObstructed() {
        var clearanceTime = rand.nextInt(MAX_RUNWAY_CLEARANCE_TIME);
        teardownUpdatingBehaviour();
        setupClearingBehaviour(clearanceTime);
        isClear = false;
        // TODO reschedule affected operations
    }

    private void setupClearingBehaviour(int clearanceTime) {
        _clearingBehaviour = new RunwayClearingBehaviour(this, TICKRATE, clearanceTime);
        addBehaviour(_clearingBehaviour);
    }

    private void teardownClearingBehaviour() {
        removeBehaviour(_clearingBehaviour);
        _clearingBehaviour = null;
    }

    private void setupUpdatingBehaviour() {
        _updatingBehaviour = new RunwayUpdatingBehaviour(this, TICKRATE);
        addBehaviour(_updatingBehaviour);
    }

    private void teardownUpdatingBehaviour() {
        removeBehaviour(_updatingBehaviour);
        _updatingBehaviour = null;
    }

    public void setClear() {
        isClear = true;
        teardownClearingBehaviour();
        setupUpdatingBehaviour();
    }

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

    public static class RunwayUpdatingBehaviour extends TickerBehaviour {
        public RunwayUpdatingBehaviour(Runway runway, int tickRate) {
            super(runway, 1000 / tickRate);
        }

        @Override
        protected void onTick() {
            var runway = (Runway) this.getAgent();
            runway.updateOperations();
        }
    }

    class ProposalBuilder extends ContractNetResponder {

        public ProposalBuilder(Agent agent, MessageTemplate mt) {
            super(agent, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException {
            var runway = (Runway) getAgent();

            System.out.println("Received CFP");

            // evaluate possibilities and make the best proposal possible
            try {
                var call = (RunwayOperationCfp) cfp.getContentObject();
                var slot = runway.getEarliestSlot(call.getMinTime());

                var proposal = RunwayOperationProposal.build(runway.getId(), slot);
                var reply = cfp.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(proposal);

                System.out.println("RUNWAY :: " + myAgent.getLocalName() + " :: Sent proposal.");

                return reply;
            } catch (Exception e) {
                e.printStackTrace();
                throw new NotUnderstoodException(e.toString());
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println("RUNWAY :: " + myAgent.getLocalName() + " :: Received an accept.");

            // realize the activity that was proposed

            // create a reply informing if the activity was completed successfully
            ACLMessage result = accept.createReply();
            result.setPerformative(ACLMessage.INFORM);
            result.setContent("this is the result");

            return result;
        }
    }
}
