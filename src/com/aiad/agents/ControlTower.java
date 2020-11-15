package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.*;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import jade.proto.SubscriptionInitiator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class ControlTower extends Agent {

    private int arrivalCounter = 0, departureCounter = 0, redirectCounter = 0;
    private int sumWaitTimeArriv = 0, sumWaitTimeDepart = 0;
    protected ArrayList<AID> runways;
    protected FileWriter allocationLog;

    public ControlTower() {
        runways = new ArrayList<>();

        File file = new File(Config.AIRPLANE_ALLOCATION_LOG);
        try {
            allocationLog = new FileWriter(file);
            allocationLog.write("airplane_id, type, accepted, runway_id, wait_time, total_arrivals, total_departures, total_redirect, avg_waitTimeArrive, avg_waitTimeDepart\n");
            allocationLog.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void incrementArrivals() {
        this.arrivalCounter++;
    }

    protected void incrementDepartures() {
        this.departureCounter++;
    }

    protected void updateSumWaitTimeArriv(int update){this.sumWaitTimeArriv += update;}

    protected void updateSumWaitTimeDepart(int update){this.sumWaitTimeDepart += update;}

    @Override
    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
        service.setName("control_tower");
        service.setType("control_tower");
        description.addServices(service);

        try {
            DFService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        DFAgentDescription runwayTemplate = new DFAgentDescription();
        ServiceDescription runwayService = new ServiceDescription();
        runwayService.setType("runway");
        runwayTemplate.addServices(runwayService);
        addBehaviour(new RunwaySubscriber(this, runwayTemplate));

        addBehaviour(new AirplaneRequestResponder(this));
    }

    public int getTotalArrivals() {
        return this.arrivalCounter;
    }

    public int getTotalDepartures() {
        return this.departureCounter;
    }

    public int getTotalRedirect(){
        return this.redirectCounter;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            allocationLog.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void logAllocation(Object content, boolean refuse, int runway, int waitTime) {
        String log = "" + ((AirplaneRequest) content).getId() + ", ";
        if (content instanceof ArrivingAirplaneRequest) {
            log += "arriving, ";
        }
        else {
            log += "departing, ";
        }
        log += (refuse ? "declined, NA, NA" : "accepted, " + runway + ", " + waitTime) + ", " + getTotalArrivals() + ", " + getTotalDepartures()+ ", " + getTotalRedirect()
                + ", " + (getTotalArrivals()==0? "NA":(sumWaitTimeArriv/getTotalArrivals())) + ", " + (getTotalDepartures()==0? "NA":(sumWaitTimeDepart/getTotalDepartures())) + "\n";

        try {
            allocationLog.write(log);
            allocationLog.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class RunwaySubscriber extends SubscriptionInitiator {
        public RunwaySubscriber(ControlTower agent, DFAgentDescription template) {
            super(agent, DFService.createSubscriptionMessage(agent, getDefaultDF(), template, null));
        }

        protected void handleInform(ACLMessage message) {
            ControlTower controlTower = (ControlTower) myAgent;

            try {
                DFAgentDescription[] dfds = DFService.decodeNotification(message.getContent());

                for (int i = 0; i < dfds.length; i++) {
                    AID agent = dfds[i].getName();
                    controlTower.runways.add(agent);
                    System.out.println("CONTROL_TOWER :: Runway added : " + agent.getLocalName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

    }

    class AirplaneRequestResponder extends CyclicBehaviour {

        public AirplaneRequestResponder(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

            ACLMessage msg = receive(msgTemplate);
            if (msg != null) {
                switch (msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        handleRequest(msg);
                        break;
                    default:
                        break;
                }
            } else block();
        }

        private void handleRequest(ACLMessage request) {
            System.out.println("CONTROL_TOWER :: Request received from : " + request.getSender().getLocalName());

            int airplaneId, minTime;


            AirplaneRequest airplaneRequest = null;
            try {
                airplaneRequest = (AirplaneRequest) request.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            airplaneId = airplaneRequest.getId();
            minTime = airplaneRequest.getTimeToArrive();

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

            RunwayOperationCfp cfpContent = new RunwayOperationCfp();
            cfpContent.setAirplaneId(airplaneId);
            cfpContent.setMinTime(minTime);

            try {
                cfp.setContentObject(cfpContent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            myAgent.addBehaviour(new RunwayAllocator(myAgent, cfp, request));
        }

    }

    class RunwayAllocator extends ContractNetInitiator {

        // the request message that started the protocol
        private ACLMessage request;

        public RunwayAllocator(Agent a, ACLMessage cfp, ACLMessage request) {
            super(a, cfp);
            this.request = request;
        }


        protected Vector prepareCfps(ACLMessage cfp) {
            ArrayList<AID> runways = ((ControlTower) myAgent).runways;
            Vector v = new Vector();

            for (int i = 0; i < runways.size(); i++) {
                cfp.addReceiver(runways.get(i));
            }

            System.out.println("CONTROL_TOWER :: CFPs prepared");

            v.add(cfp);
            return v;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            int bestProposalIndex = 0;
            int bestRunwayId = 0;
            int minOperationTime = Integer.MAX_VALUE;

            System.out.println("CONTROL_TOWER :: Received " + responses.size() + " proposals");

            try {
                minOperationTime = ((RunwayOperationProposal) ((ACLMessage) responses.get(0)).getContentObject()).getOperationTime();
                bestRunwayId = ((RunwayOperationProposal) ((ACLMessage) responses.get(0)).getContentObject()).getRunwayId();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            for (int i = 1; i < responses.size(); i++) {

                ACLMessage message = (ACLMessage) responses.get(i);
                int runwayId = -1;
                int operationTime = Integer.MAX_VALUE;
                try {
                    RunwayOperationProposal proposal = (RunwayOperationProposal) message.getContentObject();
                    runwayId = proposal.getRunwayId();
                    operationTime = proposal.getOperationTime();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                int rejectedProposalIndex = i;

                System.out.println("CONTROL_TOWER :: Comparing proposals : " + minOperationTime + " and " + operationTime);
                if (minOperationTime > operationTime) {
                    minOperationTime = operationTime;
                    rejectedProposalIndex = bestProposalIndex;
                    bestProposalIndex = i;
                    bestRunwayId = runwayId;
                }

                // create reply for rejected proposal
                ACLMessage reply = ((ACLMessage) responses.get(rejectedProposalIndex)).createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setContent("proposal rejected");
                acceptances.add(reply);
            }

            System.out.println("CONTROL_TOWER :: All proposals considered");
            System.out.println("CONTROL_TOWER :: Best proposal : " + minOperationTime);


            Object content = null;
            try {
                content = request.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            // refuse if there are no proposals or the request came from an arriving plane and the best proposal is greater than the plane's autonomy
            boolean refuse = responses.isEmpty() || (content instanceof ArrivingAirplaneRequest && minOperationTime > ((ArrivingAirplaneRequest) content).getFuelRemaining());

            if (refuse) {
                // send refuse to airplane
                sendRefuse(request.createReply());
                redirectCounter++;

                // reject the best proposal
                ACLMessage reply = ((ACLMessage) responses.get(bestProposalIndex)).createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setContent("proposal rejected");
                acceptances.add(reply);
            } else {
                // send agree and inform to airplane
                sendAgree(request.createReply());
                sendInform(request.createReply(), bestRunwayId, minOperationTime);

                // accept the best proposal
                ACLMessage reply = ((ACLMessage) responses.get(bestProposalIndex)).createReply();
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("proposal accepted");
                acceptances.add(reply);

                ControlTower controlTower = (ControlTower) myAgent;
                if (content instanceof ArrivingAirplaneRequest) {
                    controlTower.incrementArrivals();
                    controlTower.updateSumWaitTimeArriv(minOperationTime  - ((AirplaneRequest)content).getTimeToArrive());
                } else {
                    controlTower.incrementDepartures();
                    controlTower.updateSumWaitTimeDepart(minOperationTime - ((AirplaneRequest)content).getTimeToArrive());
                }
            }

            logAllocation(content, refuse, bestRunwayId, (minOperationTime - ((AirplaneRequest)content).getTimeToArrive()));
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("CONTROL_TOWER :: Received an inform message");
        }

        private void sendRefuse(ACLMessage refuse) {
            refuse.setPerformative(ACLMessage.REFUSE);
            refuse.setContent("Refuse to airplane request");
            send(refuse);
        }

        private void sendAgree(ACLMessage agree) {
            agree.setPerformative(ACLMessage.AGREE);
            agree.setContent("Agree to airplane request");
            send(agree);
        }

        private void sendInform(ACLMessage inform, int runwayId, int operationTime) {
            inform.setPerformative(ACLMessage.INFORM);
            AirplaneInform content = new AirplaneInform();
            content.setRunwayId(runwayId);
            content.setWaitTime(operationTime);
            try {
                inform.setContentObject(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            send(inform);
        }

    }

}
