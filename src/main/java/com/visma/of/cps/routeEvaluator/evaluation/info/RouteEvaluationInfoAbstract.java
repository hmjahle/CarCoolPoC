package com.visma.of.cps.routeEvaluator.evaluation.info;

import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;

public class RouteEvaluationInfoAbstract {
    // OBS: in orginal code, use Task, but we use task
    protected Visit visit;
    protected int shiftId;
    protected int endOfWorkShift;
    protected int syncedTaskStartTime;

    public RouteEvaluationInfoAbstract(Visit visit, Shift employeeWorkShift) {
        this.visit = visit;
        this.shiftId = employeeWorkShift.getId();
        this.endOfWorkShift = employeeWorkShift.getTimeWindowEnd();
    }

    public int getSyncedTaskStartTime() {
        return syncedTaskStartTime;
    }

    public Visit getVisit() {
        return visit;
    }

    public int getEndOfWorkShift() {
        return endOfWorkShift;
    }

    public boolean isStrict() {
        return visit != null && visit.getTask().isStrict();
    }

    public boolean isSynced() {
        return visit != null && visit.isSynced();
    }
    // If task is origin or destination
    public boolean isDepot() {
        return visit == null;
    }

}
