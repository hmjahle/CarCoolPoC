package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;
import com.visma.of.cps.routeEvaluator.evaluation.info.RouteEvaluationInfoAbstract;

import java.util.function.Function;

;

public class TimeWindowLateArrivalConstraint extends CustomCriteriaConstraint{

    public static final int DEFAULT_LATEST_ARRIVAL_AT_TASK = 2 * 3600;

    public TimeWindowLateArrivalConstraint() {
        super(criteriaFunction(), constraintFunction(DEFAULT_LATEST_ARRIVAL_AT_TASK));
    }

    public TimeWindowLateArrivalConstraint(int maximumLateArrival) {
        super(criteriaFunction(), constraintFunction(maximumLateArrival));
    }

    private static Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction() {
        return i -> !i.isDepot();
    }

    private static Function<ConstraintInfo, Boolean> constraintFunction(int maximumLateArrival) {
        return i -> i.getStartOfServiceNextTask() + i.getVisit().getVisitDuration() <= i.getVisit().getTimeWindowEnd() + maximumLateArrival;
    }
}
