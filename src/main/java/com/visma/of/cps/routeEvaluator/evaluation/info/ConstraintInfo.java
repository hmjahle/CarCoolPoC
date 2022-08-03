package com.visma.of.cps.routeEvaluator.evaluation.info;


import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;

/**
 * Class that contain the necessary info to evaluate the constraints.
 */
public class ConstraintInfo extends RouteEvaluationInfoAbstract {
    int earliestOfficeReturn;
    int startOfServiceNextTask;
    int shiftStartTime;
    int syncedVisitLatestStartTime;

    public ConstraintInfo(Shift employeeWorkShift, int earliestOfficeReturn, Visit visit, int startOfServiceNextTask, int syncedVisitLatestStartTime, int shiftStartTime) {
        super(visit, employeeWorkShift);
        this.earliestOfficeReturn = earliestOfficeReturn;
        this.startOfServiceNextTask = startOfServiceNextTask;
        this.syncedVisitLatestStartTime = syncedVisitLatestStartTime;
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

    
