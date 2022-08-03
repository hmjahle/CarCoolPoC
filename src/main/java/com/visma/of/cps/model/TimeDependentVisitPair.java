package com.visma.of.cps.model;

public class TimeDependentVisitPair {

    private Visit masterVisit;
    private Visit dependentVisit;
    private int intervalStart;
    private int intervalEnd;

    public TimeDependentVisitPair(Visit masterVisit, Visit dependentVisit, int intervalStart, int intervalEnd) {
        this.masterVisit = masterVisit;
        this.dependentVisit = dependentVisit;
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
    
}
