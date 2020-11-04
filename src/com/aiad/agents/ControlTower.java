package com.aiad.agents;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class ControlTower extends Agent {

    private int arrivalCounter = 0, departureCounter = 0;
    private ArrayList<Boolean> runwayState;

    public ControlTower() {
        runwayState = new ArrayList<>();
    }

    public void handleMessage(ACLMessage message) {

        switch (message.getPerformative()) {
            case ACLMessage.REQUEST:
                // can send refuse or agree
                ACLMessage reply = new ACLMessage(ACLMessage.AGREE);
                reply.setContent("Esta mensagem é um Agree para o avião");
                reply.addReceiver(message.getSender());
                reply.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                send(reply);

                ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
                notification.setContent("Esta mensagem é um INFORM para o avião");
                notification.addReceiver(message.getSender());
                notification.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                send(notification);
                break;
            default:
                System.err.println("NOT A REQUEST");
                break;
        }

    }

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //Receive message
                ACLMessage msg = receive();
                if (msg != null) {
                    handleMessage(msg);
                } else block();
            }
        });
    }

    public int getTotalArrivals() {
        return this.arrivalCounter;
    }

    public int getTotalDepartures() {
        return this.departureCounter;
    }

}
