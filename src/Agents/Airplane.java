package Agents;
import jade.core.*;

public class Airplane extends Agent {
    private int id,  waitTime = 0, timeToArrive;
    private float fuelRemaining;
    private boolean landed = false;

    public Airplane(){};

    /*
    *   The message will be the following :
    *   " id waitTime timeToArrive fuelRemaining landed"
    *
    */

    public Airplane(String message) {
        String[] splitMessage = message.split(" ");
        this.id = splitMessage[0];
        this.waitTime = splitMessage[1];
        this.timeToArrive = splitMessage[2];
        this.fuelRemaining = splitMessage[3];
        this.landed = splitMessage[4];
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