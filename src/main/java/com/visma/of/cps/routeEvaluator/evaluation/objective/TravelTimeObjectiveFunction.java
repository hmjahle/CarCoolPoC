package com.visma.of.cps.routeEvaluator.evaluation.objective;

import com.visma.of.cps.routeEvaluator.evaluation.info.ObjectiveInfo;

public class TravelTimeObjectiveFunction implements IObjectiveFunctionIntraRoute {

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        return objectiveInfo.getTravelTime();
    }
}