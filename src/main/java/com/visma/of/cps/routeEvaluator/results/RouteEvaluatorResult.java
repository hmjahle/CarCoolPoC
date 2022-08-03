package com.visma.of.cps.routeEvaluator.results;

import com.visma.of.cps.model.Visit;
import com.visma.of.cps.routeEvaluator.solver.algorithm.IRouteEvaluatorObjective;

import java.util.List;

public class RouteEvaluatorResult {

    private final Route route;
    private final IRouteEvaluatorObjective objective;

    public RouteEvaluatorResult(IRouteEvaluatorObjective objective, Route route) {
        this.route = route;
        this.objective = objective;
    }

    public Route getRoute() {
        return route;
    }

    public List<Visit> getVisitSolution() {
        return route.getVisitSolution();
    }

    public double getObjectiveValue() {
        return objective.getObjectiveValue();
    }

    public Integer getTimeOfArrivalAtDestination() {
        return route.getRouteFinishedAtTime();
    }

    public IRouteEvaluatorObjective getObjective() {
        return objective;
    }
}
