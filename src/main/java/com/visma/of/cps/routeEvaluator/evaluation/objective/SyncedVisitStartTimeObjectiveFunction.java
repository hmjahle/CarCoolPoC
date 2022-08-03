package com.visma.of.cps.routeEvaluator.evaluation.objective;

import com.visma.of.cps.routeEvaluator.evaluation.info.ObjectiveInfo;

public class SyncedVisitStartTimeObjectiveFunction implements IObjectiveFunctionIntraRoute {

    private final long allowedSlack;

    public SyncedVisitStartTimeObjectiveFunction(long allowedSlack) {
        this.allowedSlack = allowedSlack;
    }

    public SyncedVisitStartTimeObjectiveFunction() {
        this.allowedSlack = 0;
    }

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        if (!objectiveInfo.isSynced())
            return 0;
        else {
            return Math.max(0.0, (objectiveInfo.getStartOfServiceNextTask() -
                    (objectiveInfo.getSyncedTaskStartTime() + allowedSlack)));
        }
    }
}