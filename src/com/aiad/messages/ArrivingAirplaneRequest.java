package com.aiad.messages;

import java.io.Serializable;

public class ArrivingAirplaneRequest extends AirplaneRequest {

    private int fuelRemaining;

    public ArrivingAirplaneRequest() {
        super();
    }

    public int getFuelRemaining() {
        return fuelRemaining;
    }

    public void setFuelRemaining(int fuelRemaining) {
        this.fuelRemaining = fuelRemaining;
    }
}
