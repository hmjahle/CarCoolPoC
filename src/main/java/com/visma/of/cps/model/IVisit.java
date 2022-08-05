package com.visma.of.cps.model;

public interface IVisit {
    int getVisitDuration();

    Integer getTimeWindowStart();

    Integer getTimeWindowEnd();

    boolean isSynced();
}
