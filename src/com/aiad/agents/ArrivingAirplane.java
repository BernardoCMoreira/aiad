package com.aiad.agents;
import com.aiad.messages.AirplaneInform;
import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

public class ArrivingAirplane extends Agent {
    private int id, waitTime = 0, timeToArrive;
    private int fuelRemaining;
    private boolean landed = false;

    public ArrivingAirplane() {
    }

    /*
     *   The message will be the following :
     *   " id waitTime timeToArrive fuelRemaining landed"
     *
     */

    public ArrivingAirplane(String message) {
        String[] splitMessage = message.split(" ");
        this.id = Integer.parseInt(splitMessage[0]);
        this.waitTime = Integer.parseInt(splitMessage[1]);
        this.timeToArrive = Integer.parseInt(splitMessage[2]);
        this.fuelRemaining = Integer.parseInt(splitMessage[3]);
        this.landed = splitMessage[4].equals("true");
    }

    @Override
    protected void setup() {
        System.out.println("\n Created new airplane : " + getLocalName() + " with attributes : " + this.id + "\t" + this.waitTime +"\t"+  this.timeToArrive +"\t" + this.fuelRemaining + "\t" +  this.landed +  "\n");
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
        addBehaviour(new CyclicBehaviour() {
            int state = Globals.FIRST_STATE;

            @Override
            public void action() {
                //Send  Message
                if (state == Globals.FIRST_STATE) {
                    sendRequestMessage();
                    state = Globals.SECOND_STATE;
                } else {
                    handleMessagesReceived();
                    block();
                }

            }

        });

    }

    public void sendRequestMessage() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        ArrivingAirplaneRequest content = new ArrivingAirplaneRequest();
        content.setId(this.id);
        content.setEta(this.timeToArrive);
        content.setAutonomy(this.fuelRemaining);

        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.addReceiver(new AID("tower", AID.ISLOCALNAME));
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        System.out.println("Request sent!");
        send(msg);
    }

    public void handleMessagesReceived() {
        ACLMessage msg = receive();
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.AGREE:
                    handleAgree(msg);
                    break;
                case ACLMessage.INFORM:
                    handleInform(msg);
                    break;
            }
        }
    }

    public void handleAgree(ACLMessage agree) {
        System.out.println("I received an agree!");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("I received a refuse!");
    }

    public void handleInform(ACLMessage inform) {
        System.out.println("I received an inform!");
        try {
            AirplaneInform content = (AirplaneInform) inform.getContentObject();
            System.out.println(content.toString());
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    public void handleFailure(ACLMessage failure) {
        System.out.println("I received a failure!");
    }


    protected void takeDown(){
        try {
            DFService.deregister(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public int getID(){
        return this.id;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public int getTimeToArrive() {
        return this.timeToArrive;
    }

    public float getFuelRemaining() {
        return this.fuelRemaining;
    }

    public boolean isLanded() {
        return this.landed;
    }
}