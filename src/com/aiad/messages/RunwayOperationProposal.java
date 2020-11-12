package com.aiad.messages;

import java.io.Serializable;

public class RunwayOperationProposal implements Serializable {
    private int runwayId;
    private int operationTime;

    public static RunwayOperationProposal build(int runwayId, int operationTime) {
        var proposal = new RunwayOperationProposal();
        proposal.setRunwayId(runwayId);
        proposal.setOperationTime(operationTime);
        return proposal;
    }

    public int getRunwayId() {
        return runwayId;
    }

    public void setRunwayId(int runwayId) {
        this.runwayId = runwayId;
    }

    public int getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(int operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public String toString() {
        return "RunwayOperationProposal{" +
                "runwayId=" + runwayId +
                ", operationTime=" + operationTime +
                '}';
    }
}