package routeEvaluator.evaluation.info;

import model.Visit;
import model.Shift;

public class ObjectiveInfo extends RouteEvaluationInfoAbstract {
    private final int travelTime;
    private final int visitEnd;
    private final int startOfServiceNextTask;

    public ObjectiveInfo(int travelTime,  Visit visit, int visitEnd, int startOfServiceNextTask, Shift employeeWorkShift) {
        super(visit, employeeWorkShift);
        this.travelTime = travelTime;
        this.visitEnd = visitEnd;
        this.startOfServiceNextTask = startOfServiceNextTask;
    }

    @Override
    public int getEndOfWorkShift() {
        return endOfWorkShift;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getVisitEnd() {
        return visitEnd;
    }

    public int getStartOfServiceNextTask() {
        return startOfServiceNextTask;
    }

    public int getEmployeeWorkShiftId() {
        return shiftId;
    }
}
