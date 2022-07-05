package routeEvaluator.evaluation.objective;

import routeEvaluator.evaluation.info.ObjectiveInfo;

public class TravelTimeObjectiveFunction implements IObjectiveFunctionIntraRoute {

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        return objectiveInfo.getTravelTime();
    }
}