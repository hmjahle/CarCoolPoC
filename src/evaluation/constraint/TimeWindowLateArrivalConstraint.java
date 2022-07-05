package evaluation.constraint;

import evaluation.info.ConstraintInfo;
import evaluation.info.RouteEvaluationInfoAbstract;
import java.util.function.Function;;

public class TimeWindowLateArrivalConstraint extends CustomCriteriaConstraint{

    public static final int DEFAULT_LATEST_ARRIVAL_AT_TASK = 2 * 3600;

    public TimeWindowLateArrivalConstraint() {
        super(criteriaFunction(), constraintFunction(DEFAULT_LATEST_ARRIVAL_AT_TASK));
    }

    public TimeWindowLateArrivalConstraint(int maximumLateArrival) {
        super(criteriaFunction(), constraintFunction(maximumLateArrival));
    }

    private static Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction() {
        return i -> !i.isDestination();
    }

    private static Function<ConstraintInfo, Boolean> constraintFunction(int maximumLateArrival) {
        return i -> i.getStartOfServiceNextTask() + i.getTask().getDuration() <= i.getTask().getEndTime() + maximumLateArrival;
    }
}
