package evaluation.constraint;

import evaluation.routeEvaluator.RouteEvaluationInfoAbstract;
import model.Shift;
import model.Visit;

/**
 * Class that contain the necessary info to evaluate the constraints.
 */
public class ConstraintInfo extends RouteEvaluationInfoAbstract {
    int earliestOfficeReturn;
    int startOfServiceNextTask;
    int shiftStartTime;

    public ConstraintInfo(Shift employeeWorkShift, int earliestOfficeReturn, Visit visit, int startOfServiceNextTask, int shiftStartTime) {
        super(visit, employeeWorkShift);
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
