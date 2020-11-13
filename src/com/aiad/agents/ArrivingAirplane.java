package com.aiad.agents;
import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class ArrivingAirplane extends Airplane {

    private int fuelRemaining;

    public ArrivingAirplane(int id, int timeToArrive, int fuelRemaining) {
        super(id, timeToArrive);
        this.fuelRemaining = fuelRemaining;
    }


    @Override
    protected ACLMessage createRequestMessage() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        ArrivingAirplaneRequest content = new ArrivingAirplaneRequest();
        content.setId(id);
        content.setTimeToArrive(timeToArrive);
        content.setFuelRemaining(fuelRemaining);
        try {
            request.setContentObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.addReceiver(new AID("tower", AID.ISLOCALNAME));
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        return request;
    }
}