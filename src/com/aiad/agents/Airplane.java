package com.aiad.agents;
import jade.core.*;

public class Airplane extends Agent {
    private int id,  waitTime = 0, timeToArrive;
    private float fuelRemaining;
    private boolean landed = false;

    public Airplane(){}

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

    public int getID(){
        return this.id;
    }

    public int getWaitTime(){
        return this.waitTime;
    }

    public int getTimeToArrive(){
        return this.timeToArrive;
    }

    public float getFuelRemaining(){
        return this.fuelRemaining;
    }

    public boolean isLanded(){
        return this.landed;
    }
}