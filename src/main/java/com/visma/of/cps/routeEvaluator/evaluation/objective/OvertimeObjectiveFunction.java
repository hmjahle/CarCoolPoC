package com.visma.of.cps.routeEvaluator.evaluation.objective;

import com.visma.of.cps.routeEvaluator.evaluation.OvertimeAbstract;
import com.visma.of.cps.routeEvaluator.evaluation.info.ObjectiveInfo;

public class OvertimeObjectiveFunction extends OvertimeAbstract implements IObjectiveFunctionIntraRoute {

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        if (objectiveInfo.isDepot()) {
            long workShiftEnd = objectiveInfo.getEndOfWorkShift();
            long officeReturn = objectiveInfo.getStartOfServiceNextTask();
            if (isOverTime(workShiftEnd, officeReturn)) {
                return (double) (officeReturn - workShiftEnd);
            }
        }
        return 0.0;
    }

}