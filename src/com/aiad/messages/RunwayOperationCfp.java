package com.aiad.messages;

import java.io.Serializable;

public class RunwayOperationCfp implements Serializable {
    private int airplaneId;
    private int minTime;

    public static RunwayOperationCfp build(int airplaneId, int minTime) {
        var cfp = new RunwayOperationCfp();
        cfp.setAirplaneId(airplaneId);
        cfp.setMinTime(minTime);
        return cfp;
    }

    public int getAirplaneId() {
        return airplaneId;
    }

    public void setAirplaneId(int airplaneId) {
        this.airplaneId = airplaneId;
    }

    public int getMinTime() {
        return minTime;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    @Override
    public String toString() {
        return "RunwayOperationCfp{" +
                "airplaneId=" + airplaneId +
                ", minTime=" + minTime +
                '}';
    }
}
