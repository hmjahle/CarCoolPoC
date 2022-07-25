package model;

public class TimeDependentTaskPair {

    private Task masterTask;
    private Task dependentTask;
    private int intervalStart;
    private int intervalEnd;

    public TimeDependentTaskPair(Task masterTask, Task dependentTask, int intervalStart, int intervalEnd) {
        this.masterTask = masterTask;
        this.dependentTask = dependentTask;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public Task getMasterTask() {
        return masterTask;
    }

    public Task getDependentTask() {
        return dependentTask;
    }

    public int getIntervalStart() {
        return intervalStart;
    }

    public int getIntervalEnd() {
        return intervalEnd;
    }    
    
}
