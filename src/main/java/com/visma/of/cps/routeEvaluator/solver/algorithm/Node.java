package com.visma.of.cps.routeEvaluator.solver.algorithm;

// OBS: all task used to be ITask
import com.visma.of.cps.model.Visit;

public class Node {

    protected int nodeId;
    private  Visit visit;
    private int locationId;

    protected Node(int nodeId) {
        this.nodeId = nodeId;
    }

    public Node(int nodeId,  Visit visit) {
        this.visit = visit;
        this.nodeId = nodeId;
        // If there is no task connected to the visit, the visit location is depot, hence id = 0
        this.locationId = visit.getTask() == null ? 0 : visit.getTask().getLocation().getId();
    }

    public Node(Node other) {
        this.nodeId = other.nodeId;
        this.visit = other.visit;
        this.locationId = other.locationId;
    }


    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }
    public int getDurationSeconds() {
        return visit == null ? 0 : visit.getTask().getDuration();
    }

    public int getTimeWindowStart() {
        return visit == null ? 0 : visit.getTimeWindowStart();
    }
    
    @Override
    public boolean equals(Object other) {
        if ((other instanceof Node))
            return ((Node) other).nodeId == this.nodeId;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return nodeId;
    }

    @Override
    public String toString() {
        return Integer.toString(nodeId);
    }

    public  Visit getVisit() {
        return visit;
    }

    public int getNodeId() {
        return nodeId;
    }

    public boolean isDepotNode() {
        return visit == null;
    }

    public int getTransportMode(){
        return visit == null ? null : visit.getTransportType();
    }

    public boolean isSynced() {
        return visit != null && visit.isSynced();
    }
}
