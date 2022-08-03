package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;

public class SyncedTasksConstraint implements IConstraintIntraRoute {

    private final long allowedSlack;

    public SyncedTasksConstraint(long allowedSlack) {
        this.allowedSlack = allowedSlack;
    }

    public SyncedTasksConstraint() {
        allowedSlack = 0;
    }

    @Override
    public boolean constraintIsFeasible(ConstraintInfo constraintInfo) {
        if (!constraintInfo.isSynced())
            return true;
        return (constraintInfo.getStartOfServiceNextTask() <= constraintInfo.getSyncedTaskStartTime()
                //+ constraintInfo.getVisit().getSyncedWithIntervalDiff()
                + allowedSlack);
    }
}

