package com.aiad.messages;

import java.io.Serializable;

public class AirplaneInform implements Serializable {

    private int runwayId, waitTime;

    public AirplaneInform() {}


    public int getRunwayId() {
        return runwayId;
    }

    public void setRunwayId(int runwayId) {
        this.runwayId = runwayId;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public String toString() {
        return "AirplaneInform{" + "runwayId=" + runwayId + ", waitTime=" + waitTime + '}';
    }
}
