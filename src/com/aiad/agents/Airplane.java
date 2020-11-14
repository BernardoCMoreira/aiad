package com.aiad.agents;

import com.aiad.messages.AirplaneInform;
import com.aiad.messages.AirplaneRequest;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
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

    protected final int id;
    protected int waitTime, timeToArrive;

    public Airplane(int id, int timeToArrive) {
        this.id = id;
        this.timeToArrive = timeToArrive;
        this.waitTime = 0;
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
    public void setTimeToArrive(int timeUpdated){timeToArrive = timeUpdated;}

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
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }

        // setup the request message
        ACLMessage request = createRequestMessage();
        addBehaviour(new AirplaneRequestInitiator(this, request));
        addBehaviour(new TickerBehaviour(this, 1000){
            @Override
            protected void onTick() {
                if(getTimeToArrive() > 0 ){
                setTimeToArrive(getTimeToArrive() - 1);
                System.out.println("Airplane : " + getId()  + " \tTime to Arrive : " + getTimeToArrive());
                }
            }
        });
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

                //Wait time already ensures the runway time and the time to arrive
                setTimeToArrive(content.getWaitTime());

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
