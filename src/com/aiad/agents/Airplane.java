package com.aiad.agents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Airplane extends Agent {
    private int id, waitTime = 0, timeToArrive;
    private float fuelRemaining;
    private boolean landed = false;

    public Airplane() {
    }

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

    @Override
    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
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
        msg.setContent("Esta mensagem é para a torre de controlo");
        msg.addReceiver(new AID("tower", AID.ISLOCALNAME));
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        System.out.println("Message sent : " + msg);
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