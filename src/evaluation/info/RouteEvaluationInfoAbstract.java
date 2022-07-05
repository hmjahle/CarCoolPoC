package evaluation.info;

import model.Task;
import model.Visit;
import model.Shift;

public class RouteEvaluationInfoAbstract {
    // OBS: in orginal code, use Task, but we use task
    protected Visit visit;
    protected int shiftId;
    protected int endOfWorkShift;
    protected int syncedTaskStartTime;

    public RouteEvaluationInfoAbstract(Visit visit, Shift employeeWorkShift) {
        this.visit = visit;
        this.shiftId = employeeWorkShift.getId();
        this.endOfWorkShift = employeeWorkShift.getEndTime();
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

    // If task is origin or destination
    public boolean isDepot() {
        return visit == null;
    }

}
