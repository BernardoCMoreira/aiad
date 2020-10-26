package com.aiad.agents;
import jade.core.*;

import java.util.ArrayList;

public class ControlTower extends Agent {

    private int arrivalCounter = 0, departureCounter = 0;
    private ArrayList<Boolean> runwayState;

    public ControlTower() {
        runwayState = new ArrayList<>();
    }


}
