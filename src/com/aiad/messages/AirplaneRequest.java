package com.aiad.messages;

import java.io.Serializable;

public class AirplaneRequest implements Serializable {

    private int id;
    private int timeToArrive;

    public AirplaneRequest() {}


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeToArrive() {
        return timeToArrive;
    }

    public void setTimeToArrive(int timeToArrive) {
        this.timeToArrive = timeToArrive;
    }
}
