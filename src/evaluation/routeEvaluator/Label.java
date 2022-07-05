package evaluation.routeEvaluator;

import evaluation.routeEvaluator.solver.algorithm.Node;

public class Label implements Comparable<Label> {
    private final Label previous;
    private final Node node;
    private final IRouteEvaluatorObjective objective;
    private final IResource resources;
    private final int currentLocationId;
    private final int currentTime;
    private final int travelTime;
    private int canLeaveLocationAtTime;
    private boolean closed;
    private int shiftStartTime;

    public void setCanLeaveLocationAtTime(int canLeaveLocationAtTime) {
        this.canLeaveLocationAtTime = canLeaveLocationAtTime;
    }

    public Label(Label previous, Node currentNode, int currentLocationId, IRouteEvaluatorObjective objective,
                 IResource resources, int currentTime, int travelTime) {
        this.previous = previous;
        this.node = currentNode;
        this.objective = objective;
        this.resources = resources;
        this.currentLocationId = currentLocationId;
        this.currentTime = currentTime;
        this.canLeaveLocationAtTime = currentTime;
        this.travelTime = travelTime;
        this.closed = false;
        setShiftStartTime();
    }

    public void setShiftStartTime() {
        if (previous != null) {
            if (previous.getPrevious() == null) {
                shiftStartTime = currentTime - travelTime;
            } else {
                shiftStartTime = previous.getShiftStartTime();
            }
        }
    }

    public int getShiftStartTime() {
        return shiftStartTime;
    }

    public Label getPrevious() {
        return previous;
    }

    public Node getNode() {
        return node;
    }

    public boolean isClosed() {
        return closed;
    }

    public IRouteEvaluatorObjective getObjective() {
        return objective;
    }

    /**
     * The start of service of the current task.
     *
     * @return Time in seconds
     */
    public int getCurrentTime() {
        return currentTime;
    }

    public IResource getResources() {
        return resources;
    }

    public void close() {
        closed = true;
    }

    public int getCurrentLocationId() {
        return currentLocationId;
    }

    /**
     * The time from which it is possible to calculate the travel time from a physical location to the next.
     * E.g. it is the start of service time of a task + duration of all tasks performed at that location
     * or on the way to the next location.
     *
     * @return Time in seconds
     */
    public int getCanLeaveLocationAtTime() {
        return canLeaveLocationAtTime;
    }

    /**
     * Travel time from the previous location.
     *
     * @return Time in seconds
     */
    public int getTravelTime() {
        return travelTime;
    }

    /**
     * Check if a label is being dominated by another label. Return -1 if this is dominating, 0 if equal,
     * 1 if other dominates and 2 if neither dominates.
     *
     * @param other Label being compared to this label.
     * @return Integer indicating which label is dominated.
     */
    public int dominates(Label other) {
        int compResources = this.resources.dominates(other.resources);
        if (compResources == 2)
            return 2;

        int compCurrentTime = Integer.compare(this.currentTime, other.currentTime);
        int compCanLeaveLocationAtTime = Integer.compare(this.canLeaveLocationAtTime, other.canLeaveLocationAtTime);
        int compObjective = this.objective.dominates(other.objective);

        if (compCurrentTime == 0 && compObjective == 0 && compResources == 0 && compCanLeaveLocationAtTime == 0)
            return 0;
        else if (compCurrentTime <= 0 && compObjective <= 0 && compResources <= 0 && compCanLeaveLocationAtTime <= 0)
            return -1;
        else if (compCurrentTime >= 0 && compObjective >= 0 && compResources >= 0 && compCanLeaveLocationAtTime >= 0)
            return 1;
        else
            return 2;
    }


    @Override
    public int compareTo(Label other) {
        return Double.compare(objective.getObjectiveValue(), other.objective.getObjectiveValue());
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof Label)
            return this.dominates(((Label) other)) == 0;
        return false;
    }


    @Override
    public int hashCode() {
        int hash = currentTime;
        hash += (canLeaveLocationAtTime << 2);
        hash += (travelTime << 4);
        hash += node != null ? (node.getNodeId() << 6) : 0;
        hash += node != null ? (node.getLocationId() << 8) : 0;
        hash += objective != null ? ((int) objective.getObjectiveValue() << 10) : 0;
        hash += resources != null ? resources.hashCode() : 0;
        return hash;
    }

    @Override
    public String toString() {
        int nodeId = node != null ? node.getNodeId() : -1;
        double objectiveValue = objective != null ? objective.getObjectiveValue() : 0.0;
        String resourcesString = (resources != null ? " " + resources.toString() : "");
        return nodeId + ", " + objectiveValue + resourcesString;
    }

}


