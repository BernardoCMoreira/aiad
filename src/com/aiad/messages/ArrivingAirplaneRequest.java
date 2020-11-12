package com.aiad.messages;

import java.io.Serializable;

public class ArrivingAirplaneRequest implements Serializable {

    private int id;
    private int eta;
    private int autonomy;

    public ArrivingAirplaneRequest() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public int getAutonomy() {
        return autonomy;
    }

    public void setAutonomy(int autonomy) {
        this.autonomy = autonomy;
    }
}
