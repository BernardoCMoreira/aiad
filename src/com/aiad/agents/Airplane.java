package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.AirplaneInform;
import com.aiad.messages.AirplaneRequest;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sajas.core.Agent;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import sajas.proto.AchieveREInitiator;

import java.io.IOException;
import java.util.Vector;

public class Airplane extends Agent {

    protected final int id;
    protected int waitTime, timeToArrive, totalTime;

    public Airplane(int id, int timeToArrive) {
        this.id = id;
        this.timeToArrive = timeToArrive;
        this.waitTime = 0;
        this.totalTime = 0;
    }

    public int getId() {
        return id;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getTimeToArrive() {
        return timeToArrive;
    }

    //setter
    public void setTimeToArrive(int timeUpdated) {
        timeToArrive = timeUpdated;
    }

    protected void log(String message) {
        System.out.println("AIRPLANE :: airplane" + id + " :: " + message);
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    protected ACLMessage createRequestMessage() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        AirplaneRequest content = new AirplaneRequest();
        content.setId(id);
        content.setTimeToArrive(timeToArrive);
        try {
            request.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.addReceiver(new AID("tower", AID.ISLOCALNAME));
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        return request;
    }

    @Override
    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
        service.setName("airplane");
        service.setType("airplane");
        description.addServices(service);

        try {
            DFService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // setup the request message
        ACLMessage request = createRequestMessage();
        addBehaviour(new AirplaneRequestInitiator(this, request));
        addBehaviour(new TickerBehaviour(this, Config.PERIOD) {
            @Override
            protected void onTick() {
                if (getTotalTime() > 0) {
                    setTotalTime(getTotalTime() - 1);
                    System.out.println("Airplane : " + getId() + " \tTotal Operation Time: " + getTotalTime());
                }
            }
        });
    }


    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AirplaneRequestInitiator extends AchieveREInitiator {

        public AirplaneRequestInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        @Override
        protected Vector prepareRequests(ACLMessage request) {
            Vector<ACLMessage> v = new Vector<>();
            v.add(request);
            return v;
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            log("Received an agree.");
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            log("Received a refuse.");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            log("Received an inform.");
            try {
                AirplaneInform content = (AirplaneInform) inform.getContentObject();
                log(content.toString());
                //Wait time already ensures the runway time and the time to arrive
                setTimeToArrive(content.getWaitTime());
                setTotalTime(content.getWaitTime() + Config.OPERATION_LENGTH);


                getAgent().addBehaviour(new Behaviour(getAgent()) {
                    private boolean cancelled = false;

                    @Override
                    public void action() {
                        var template = new MessageTemplate(
                                (MessageTemplate.MatchExpression) msg -> msg.getContent().equals("Cancelled")
                        );

                        var message = receive(template);
                        if (message == null) return;

                        var airplane = (Airplane) getAgent();
                        airplane.addBehaviour(new AirplaneRequestInitiator(airplane, airplane.createRequestMessage()));
                        cancelled = true;
                    }

                    @Override
                    public boolean done() {
                        return cancelled;
                    }
                });
                getAgent().removeBehaviour(this);
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            log("Received a failure.");
        }
    }
}
