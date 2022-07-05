package evaluation.routeEvaluator.results;

import model.Visit;

import java.util.List;

import evaluation.routeEvaluator.solver.algorithm.IRouteEvaluatorObjective;

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
