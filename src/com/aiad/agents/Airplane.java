package com.aiad.agents;

import com.aiad.messages.AirplaneInform;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Airplane extends Agent {

    private int id, waitTime = 0, timeToArrive;
    private int fuelRemaining;
    private boolean landed = false;


    /*
     *   The message will be the following :
     *   " id waitTime timeToArrive fuelRemaining landed"
     *
     */

    public Airplane(String message) {
        String[] splitMessage = message.split(" ");
        this.id = Integer.parseInt(splitMessage[0]);
        this.waitTime = Integer.parseInt(splitMessage[1]);
        this.timeToArrive = Integer.parseInt(splitMessage[2]);
        this.fuelRemaining = Integer.parseInt(splitMessage[3]);
        this.landed = splitMessage[4].equals("true");
    }

    // Getters

    public int getId() {
        return id;
    }

    public int getFuelRemaining() {
        return fuelRemaining;
    }


    public int getWaitTime() {
        return waitTime;
    }

    public int getTimeToArrive() {
        return timeToArrive;
    }

    public boolean isLanded(){
        return landed;
    }

    // Handlers
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
}
