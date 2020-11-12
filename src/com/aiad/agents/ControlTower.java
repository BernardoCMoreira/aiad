package com.aiad.agents;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFSubscriber;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.SubscriptionInitiator;

import java.lang.module.FindException;
import java.util.ArrayList;
import java.util.Vector;

public class ControlTower extends Agent {

    private int arrivalCounter = 0, departureCounter = 0;
    protected ArrayList<AID> runways;

    public ControlTower() {
        runways = new ArrayList<>();
    }

    public void handleMessage(ACLMessage message) {

        switch (message.getPerformative()) {
            case ACLMessage.REQUEST:
                // can send refuse or agree
                // Send agree
                ACLMessage reply = new ACLMessage(ACLMessage.AGREE);
                reply.setContent("Esta mensagem é um Agree para o avião");
                reply.addReceiver(message.getSender());
                reply.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                send(reply);
                //Send notification
                ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
                notification.setContent("Esta mensagem é um INFORM para o avião");
                notification.addReceiver(message.getSender());
                notification.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                send(notification);
                break;
            default:
                System.err.println("NOT A REQUEST");
                System.out.println(message);
                break;
        }
    }

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

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                //Receive message
                ACLMessage msg = receive(msgTemplate);
                if (msg != null) {
                    handleMessage(msg);
                } else block();
            }
        });

        DFAgentDescription runwayTemplate = new DFAgentDescription();
        ServiceDescription runwayService = new ServiceDescription();
        runwayService.setType("runway");
        runwayTemplate.addServices(runwayService);

        addBehaviour(new RunwaySubscriber(this, runwayTemplate));
    }

    public int getTotalArrivals() {
        return this.arrivalCounter;
    }

    public int getTotalDepartures() {
        return this.departureCounter;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
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
                    System.out.println("new runway added : " + agent.getLocalName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

    }

    class RunwayAllocator extends ContractNetInitiator {
        public RunwayAllocator(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            ArrayList<AID> runways = ((ControlTower) myAgent).runways;
            Vector v = new Vector();

            for (int i = 0; i < runways.size(); i++) {
                cfp.addReceiver(runways.get(i));
            }

            v.add(cfp);
            return v;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            int bestProposalIndex = 0;

            for (int i = 1; i < responses.size(); i++) {
                // compare best proposal with current proposal
                boolean isBetter = true;
                int rejectedProposalIndex = i;

                if (isBetter) {
                    rejectedProposalIndex = bestProposalIndex;      // previous best proposal index
                    bestProposalIndex = i;                          // new best proposal index
                }

                // create reply for rejected proposal
                ACLMessage msg = ((ACLMessage) responses.get(rejectedProposalIndex)).createReply();
                msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.add(msg);
            }

            // create reply for accepted proposal
            ACLMessage msg = ((ACLMessage) responses.get(bestProposalIndex)).createReply();
            msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(msg);
        }

        // TODO: method to receive inform messages related to the activity
    }
}
