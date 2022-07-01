package evaluation.routeEvaluator;

import model.Task;
import model.Shift;

public class RouteEvaluationInfoAbstract {
    // OBS: in orginal code, use Task, but we use task
    protected Task task;
    protected int shiftId;
    protected int endOfWorkShift;
    protected int syncedTaskStartTime;

    public RouteEvaluationInfoAbstract(Task task, Shift employeeWorkShift, int syncedTaskStartTime) {
        this.task = task;
        this.shiftId = employeeWorkShift.getId();
        this.syncedTaskStartTime = syncedTaskStartTime;
        this.endOfWorkShift = employeeWorkShift.getEndTime();
    }

    public int getSyncedTaskStartTime() {
        return syncedTaskStartTime;
    }

    public Task getTask() {
        return task;
    }

    public int getEndOfWorkShift() {
        return endOfWorkShift;
    }

    public boolean isStrict() {
        return task != null && task.isStrict();
    }

    /* public boolean isSynced() {
        return task != null && task.isSynced();
    }
    */
    public boolean isDestination() {
        return task == null;
    }

}
