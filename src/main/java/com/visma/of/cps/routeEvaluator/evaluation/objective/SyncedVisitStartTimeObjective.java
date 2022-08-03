package com.visma.of.cps.routeEvaluator.evaluation.objective;

import com.visma.of.cps.routeEvaluator.evaluation.info.ObjectiveInfo;

public class SyncedVisitStartTimeObjective  extends SyncedVisitStartTimeObjectiveFunction{

    private final double penalty;

    public SyncedVisitStartTimeObjective(long allowedSlack, double penalty) {
        super(allowedSlack);
        this.penalty = penalty;
    }

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        return penalty * super.calculateIncrementalObjectiveValueFor(objectiveInfo);
    }

}