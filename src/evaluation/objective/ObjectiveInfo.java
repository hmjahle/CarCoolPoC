package evaluation.objective;

import model.Task;
import evaluation.routeEvaluator.RouteEvaluationInfoAbstract;
import model.Shift;

public class ObjectiveInfo extends RouteEvaluationInfoAbstract {
    private final int travelTime;
    private final int visitEnd;
    private final int startOfServiceNextTask;

    public ObjectiveInfo(int travelTime,  Task task, int visitEnd, int startOfServiceNextTask, int syncedTaskStartTime, Shift employeeWorkShift) {
        super(task, employeeWorkShift, syncedTaskStartTime);
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
/* 
    public static void main(String[] args) {
        Task task = new Task();
        Shift shift = new Shift((int) 2);
        ObjectiveInfo ob = new ObjectiveInfo(12, task, 123, 168, 123456, shift);
        System.out.println("All ok");
    } */
}
