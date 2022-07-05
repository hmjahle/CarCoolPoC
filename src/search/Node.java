package search;

// OBS: all task used to be ITask
import model.Visit;

public class Node {

    protected int nodeId;
    private  Visit visit;
    private int locationId;

    protected Node(int nodeId) {
        this.nodeId = nodeId;
    }

    public Node(int nodeId,  Visit visit, int locationId) {
        this.visit = visit;
        this.nodeId = nodeId;
        this.locationId = locationId;
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

    /* public boolean getRequirePhysicalAppearance() {
        return visit == null || visit.getRequirePhysicalAppearance();
    }

    public boolean isSynced() {
        return visit != null && visit.isSynced();
    } */

    public int getStartTime() {
        return visit == null ? 0 : visit.getStartTime();
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

    public Visit getTask() {
        return visit;
    }

    public int getNodeId() {
        return nodeId;
    }
}
