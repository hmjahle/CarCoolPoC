package evaluation.info;

import model.Task;
import model.Shift;

public class RouteEvaluationInfoAbstract {

    protected Task task;
    protected int shiftId;
    protected int endOfWorkShift;

    public RouteEvaluationInfoAbstract(Task task, Shift employeeWorkShift, long syncedTaskStartTime) {
        this.task = task;
        this.shiftId = employeeWorkShift.getId();
        this.endOfWorkShift = employeeWorkShift.getEndTime();
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

    public boolean isDestination() {
        return task == null;
    }
    
}
