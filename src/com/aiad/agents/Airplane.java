package com.aiad.agents;

import com.aiad.messages.AirplaneInform;
import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.io.IOException;
import java.util.Vector;

public class Airplane extends Agent {

    // private int id;
    // private int waitTime;

    private final int id;
    private int waitTime, timeToArrive;


    /*
     *   The message will be the following :
     *   " id waitTime timeToArrive fuelRemaining landed"
     *
     */

    public Airplane(int id, int timeToArrive) {
        this.id = id;
        this.timeToArrive = timeToArrive;
        this.waitTime = 0;
    }

    // Getters

    public int getId() {
        return id;
    }


    public int getWaitTime() {
        return waitTime;
    }

    public int getTimeToArrive() {
        return timeToArrive;
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
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        // setup the request message
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        // TODO: change this content
        ArrivingAirplaneRequest content = new ArrivingAirplaneRequest();
        content.setId(id);
        content.setEta(timeToArrive);
        try {
            request.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.addReceiver(new AID("tower", AID.ISLOCALNAME));
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        addBehaviour(new AirplaneRequestInitiator(this, request));
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch(Exception e) {
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
            System.out.println("Received an agree message.");
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Received a refuse message.");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Received an inform message.");
            try {
                AirplaneInform content = (AirplaneInform) inform.getContentObject();
                System.out.println(content.toString());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            System.out.println("Received a failure message.");
        }
    }
}
