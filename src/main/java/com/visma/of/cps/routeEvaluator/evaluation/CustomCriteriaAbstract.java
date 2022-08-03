package com.visma.of.cps.routeEvaluator.evaluation;

import com.visma.of.cps.routeEvaluator.evaluation.info.RouteEvaluationInfoAbstract;

import java.util.function.Function;

public class CustomCriteriaAbstract {

    private final Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction;

    public CustomCriteriaAbstract(Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction) {
        this.criteriaFunction = criteriaFunction;
    }

    protected boolean criteriaIsFulfilled(RouteEvaluationInfoAbstract info) {
        return criteriaFunction.apply(info);
    }
    
}
