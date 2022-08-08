package com.visma.of.cps.model;

public class TimeDependentVisitPair {

    private Visit masterVisit;
    private int masterShiftId;
    private Visit dependentVisit;
    private int dependentShiftId;
    private int intervalStart;
    private int intervalEnd;

    public TimeDependentVisitPair(Visit masterVisit, int masterShiftId, Visit dependentVisit, int dependentShiftId, int intervalStart, int intervalEnd) {
        this.masterVisit = masterVisit;
        this.dependentVisit = dependentVisit;

        this.masterShiftId = masterShiftId;
        this.dependentShiftId = dependentShiftId;

        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public Visit getMasterVisit() {
        return masterVisit;
    }

    public Visit getDependentVisit() {
        return dependentVisit;
    }

    public int getIntervalStart() {
        return intervalStart;
    }

    public int getIntervalEnd() {
        return intervalEnd;
    }

    public int getMasterShiftId() {
        return masterShiftId;
    }

    public int getDependentShiftId() {
        return dependentShiftId;
    }
}
