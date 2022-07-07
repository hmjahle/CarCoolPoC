package routeEvaluator.evaluation.objective;

import routeEvaluator.evaluation.info.ObjectiveInfo;

public class StrictTimeWindowObjectiveFunction implements IObjectiveFunctionIntraRoute {

    /**
     * Cut off at which the high penalty is applied.
     */
    private final double penalty;

    public StrictTimeWindowObjectiveFunction(double penalty) {
        this.penalty = penalty;
    }

    /**
     * Multiplier applied outside the cut off, hence breaking the time window by more than
     * the cut off is penalized x times more, than inside the cut off.
     */
    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        if (objectiveInfo.isDepot() || objectiveInfo.isStrict())
            return 0;
        else {
            long timeWindowBreak = objectiveInfo.getVisitEnd() - objectiveInfo.getVisit().getEndTime();
            return penalty * Math.max(0, timeWindowBreak);
        }
    }
}
