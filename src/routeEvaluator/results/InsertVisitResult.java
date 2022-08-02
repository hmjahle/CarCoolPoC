package routeEvaluator.results;

import java.util.List;
import java.util.Map;

import model.TimeDependentVisitPair;
import model.Visit;
import solution.Objective;

public class InsertVisitResult {

    private RouteEvaluatorResult routeEvaluatorOne;
    private RouteEvaluatorResult routeEvaluatorTwo;
    private int shiftIdOne;
    private int shiftIdTwo;
    private List<TimeDependentVisitPair> newTimeDependentVisitPairs;
    private Map<Visit, Integer> carpoolSyncedVisitStartTime;
    private boolean multipleRoutesAffected = false;

    public InsertVisitResult(RouteEvaluatorResult routeEvaluatorOne, RouteEvaluatorResult routeEvaluatorTwo,
            List<TimeDependentVisitPair> newTimeDependentVisitPairs,
            Map<Visit, Integer> carpoolTimeDependentVisitStartTime,
            int shiftIdOne, int shiftIdTwo) {
        this.routeEvaluatorOne = routeEvaluatorOne;
        this.routeEvaluatorTwo = routeEvaluatorTwo;
        this.newTimeDependentVisitPairs = newTimeDependentVisitPairs;
        this.carpoolSyncedVisitStartTime = carpoolTimeDependentVisitStartTime;
        this.multipleRoutesAffected = true;
        this.shiftIdOne = shiftIdOne;
        this.shiftIdTwo = shiftIdTwo;
    }

    public InsertVisitResult(RouteEvaluatorResult routeEvaluatorOne, int shiftIdOne) {
        this.routeEvaluatorOne = routeEvaluatorOne;
        this.shiftIdOne = shiftIdOne;
    }

    public Map<Visit, Integer> getCarpoolSyncedVisitStartTime() {
        return carpoolSyncedVisitStartTime;
    }

    public RouteEvaluatorResult getRouteEvaluatorOne() {
        return routeEvaluatorOne;
    }

    public RouteEvaluatorResult getRouteEvaluatorTwo() {
        return routeEvaluatorTwo;
    }

    public List<TimeDependentVisitPair> getNewTimeDependentVisitPairs() {
        return newTimeDependentVisitPairs;
    }

    public boolean isMultipleRoutesAffected() {
        return multipleRoutesAffected;
    }

    public Double getDeltaObjective(Objective previousObjective){
        if (isMultipleRoutesAffected()){
            return (routeEvaluatorOne.getObjectiveValue()-previousObjective.getShiftIntraRouteObjectiveValue(shiftIdOne)) + (routeEvaluatorTwo.getObjectiveValue() - previousObjective.getShiftIntraRouteObjectiveValue(shiftIdTwo));
        }
        return (routeEvaluatorOne.getObjectiveValue()-previousObjective.getShiftIntraRouteObjectiveValue(shiftIdOne));
    }

    public boolean isInfeasibleInsert(){
        if (isMultipleRoutesAffected()){
            return routeEvaluatorOne == null || routeEvaluatorTwo == null;
        }
        return routeEvaluatorOne == null;
    }

    

}
