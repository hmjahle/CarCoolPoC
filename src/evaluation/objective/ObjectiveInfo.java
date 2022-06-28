package evaluation.objective;

import model.Task;
import evaluation.routeEvaluator.RouteEvaluationInfoAbstract;
import model.Shift;

public class ObjectiveInfo extends RouteEvaluationInfoAbstract {
    private final long travelTime;
    private final long visitEnd;
    private final long startOfServiceNextTask;

    public ObjectiveInfo(long travelTime,  Task task, long visitEnd, long startOfServiceNextTask, long syncedTaskStartTime, Shift employeeWorkShift) {
        super(task, employeeWorkShift, syncedTaskStartTime);
        this.travelTime = travelTime;
        this.visitEnd = visitEnd;
        this.startOfServiceNextTask = startOfServiceNextTask;
    }

    @Override
    public long getEndOfWorkShift() {
        return endOfWorkShift;
    }

    public long getTravelTime() {
        return travelTime;
    }

    public long getVisitEnd() {
        return visitEnd;
    }

    public long getStartOfServiceNextTask() {
        return startOfServiceNextTask;
    }

    public short getEmployeeWorkShiftId() {
        return shiftId;
    }
/* 
    public static void main(String[] args) {
        Task task = new Task();
        Shift shift = new Shift((short) 2);
        ObjectiveInfo ob = new ObjectiveInfo(12, task, 123, 168, 123456, shift);
        System.out.println("All ok");
    } */
}
