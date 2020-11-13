package com.aiad.agents;

import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class DepartingAirplane extends Airplane{

    public DepartingAirplane(String message) {
        super(message);
    }

    @Override
    protected void setup() {
        System.out.println("\n Created new departing airplane : " + getLocalName() + " with attributes : " + getId()+ "\t" + getWaitTime() +"\t"+  getTimeToArrive() +"\t" + getFuelRemaining() + "\t" +  isLanded() +  "\n");
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
        content.setId(getId());
        content.setEta(getTimeToArrive());
        content.setAutonomy(getFuelRemaining());

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

    protected void takeDown(){
        try {
            DFService.deregister(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
