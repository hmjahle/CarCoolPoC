package search;

import model.Task;

/**
 * Unknown location nodes can be used in the labelling algorithms as origin and destination nodes where the location is
 * not yet determined. This can be in the case where the route has to start at the first task or end at the last task.
 */
public class VirtualNode extends Node {

    public VirtualNode(int nodeId) {
        super(nodeId);
    }

    @Override
    public int getLocationId() {
        return -1;
    }

    /* @Override

    public boolean getRequirePhysicalAppearance() {
        return false;
    } */

    @Override
    public int getDurationSeconds() {
        return 0;
    }

    @Override
    public int getStartTime() {
        return 0;
    }

   /*  @Override
    public boolean isSynced() {
        return false;
    } */

    @Override
    public Task getTask() {
        return null;
    }

    @Override
    public int hashCode() {
        return nodeId;
    }

    @Override
    public boolean equals(Object other) {
        if ((other instanceof VirtualNode))
            return ((VirtualNode) other).nodeId == this.nodeId;
        else
            return false;
    }
}
