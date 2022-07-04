package evaluation.info;


import model.Shift;
import model.Task;

/**
 * Class that contain the necessary info to evaluate the constraints.
 */
public class ConstraintInfo extends RouteEvaluationInfoAbstract {
    int earliestOfficeReturn;
    int startOfServiceNextTask;
    int shiftStartTime;

    public ConstraintInfo(Shift employeeWorkShift, int earliestOfficeReturn, Task task, int startOfServiceNextTask, int syncedTaskLatestStartTime, int shiftStartTime) {
        super(task, employeeWorkShift, syncedTaskLatestStartTime);
        this.earliestOfficeReturn = earliestOfficeReturn;
        this.startOfServiceNextTask = startOfServiceNextTask;
        //this.syncedTaskStartTime = syncedTaskLatestStartTime;
        this.shiftStartTime = shiftStartTime;
    }

    public int getEarliestOfficeReturn() {
        return earliestOfficeReturn;
    }

    public int getStartOfServiceNextTask() {
        return startOfServiceNextTask;
    }

    public int getShiftId() {
        return shiftId;
    }

    public int getShiftStartTime() {
        return shiftStartTime;
    }
}

    
