package com.aiad.agents;
import com.aiad.messages.AirplaneInform;
import com.aiad.messages.ArrivingAirplaneRequest;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.io.IOException;

public class ArrivingAirplane extends Airplane {

    private int autonomy;

    public ArrivingAirplane(int id, int timeToArrive, int autonomy) {
        super(id, timeToArrive);
        this.autonomy = autonomy;
    }

}