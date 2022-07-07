package routeEvaluator.evaluation.objective;

import routeEvaluator.evaluation.info.ObjectiveInfo;

public class TimeWindowLowHighObjectiveFunction implements IObjectiveFunctionIntraRoute {

    public static final double DEFAULT_PENALTY_FOR_BREAKING_TIME_WINDOW = .5;
    public static final double DEFAULT_PENALTY_FOR_BREAKING_TIME_WINDOW_HIGH = 2;
    public static final long DEFAULT_PENALTY_FOR_HIGH_TIME_WINDOW_CUT_OFF = 600;

    /**
     * Multiplier applied inside the cut off, hence breaking the time window by more than
     * the cut off is penalized x times more, than inside the cut off.
     */
    private final double lowPenaltyMultiplier;
    /**
     * Cut off at which the high penalty is applied.
     */
    private final long highPenaltyCutOff;
    /**
     * Multiplier applied outside the cut off, hence breaking the time window by more than
     * the cut off is penalized x times more, than inside the cut off.
     */
    private final double highPenaltyMultiplier;

    public TimeWindowLowHighObjectiveFunction(long highPenaltyCutOff, double highPenaltyMultiplier) {
        this.highPenaltyCutOff = highPenaltyCutOff;
        this.highPenaltyMultiplier = highPenaltyMultiplier;
        this.lowPenaltyMultiplier = DEFAULT_PENALTY_FOR_BREAKING_TIME_WINDOW;
    }

    public TimeWindowLowHighObjectiveFunction(double lowPenaltyMultiplier, long highPenaltyCutOff, double highPenaltyMultiplier) {
        this.highPenaltyCutOff = highPenaltyCutOff;
        this.highPenaltyMultiplier = highPenaltyMultiplier;
        this.lowPenaltyMultiplier = lowPenaltyMultiplier;
    }

    public TimeWindowLowHighObjectiveFunction() {
        this.highPenaltyCutOff = DEFAULT_PENALTY_FOR_HIGH_TIME_WINDOW_CUT_OFF;
        this.highPenaltyMultiplier = DEFAULT_PENALTY_FOR_BREAKING_TIME_WINDOW_HIGH;
        this.lowPenaltyMultiplier = DEFAULT_PENALTY_FOR_BREAKING_TIME_WINDOW;
    }

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        if (objectiveInfo.isDepot())
            return 0;
        else {
            long timeWindowBreak = objectiveInfo.getVisitEnd() - objectiveInfo.getVisit().getTaskEndTime();
            if (timeWindowBreak <= highPenaltyCutOff)
                return (long) (lowPenaltyMultiplier * Math.max(0, timeWindowBreak));
            else {
                long highPenaltyTimeWindowBreak = timeWindowBreak - highPenaltyCutOff;
                return (long) (lowPenaltyMultiplier * highPenaltyCutOff + highPenaltyTimeWindowBreak * highPenaltyMultiplier);
            }
        }
    }
}