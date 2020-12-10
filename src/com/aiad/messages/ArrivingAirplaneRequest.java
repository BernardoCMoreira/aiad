package com.aiad.messages;

import java.io.Serializable;

public class ArrivingAirplaneRequest extends AirplaneRequest {

    private Integer fuelRemaining;

    public ArrivingAirplaneRequest() {
        super();
    }

    public int getFuelRemaining() {
        return fuelRemaining;
    }

    public void setFuelRemaining(Integer fuelRemaining) {
        this.fuelRemaining = fuelRemaining;
    }
}
