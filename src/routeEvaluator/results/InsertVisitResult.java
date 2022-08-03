package routeEvaluator.results;

import java.util.ArrayList;
import java.util.HashMap;
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
    private Map<Integer, List<Visit>> insertedVisits = new HashMap<>();

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
        this.insertedVisits = new HashMap<>();
    }

    public InsertVisitResult(RouteEvaluatorResult routeEvaluatorOne, int shiftIdOne, List<Visit> insertVisits) {
        this.routeEvaluatorOne = routeEvaluatorOne;
        this.shiftIdOne = shiftIdOne;
        this.insertedVisits = new HashMap<>();
        setInsertedVisits(shiftIdOne, insertVisits);
    }
    public void setInsertedVisits(int shiftId, List<Visit> insertVisits){
        this.insertedVisits.put(shiftId, insertVisits);
    }

    public Map<Visit, Integer> getCarpoolSyncedVisitStartTime() {
        return carpoolSyncedVisitStartTime;
    }

    public int getShiftIdOne() {
        return this.shiftIdOne;
    }

    public int getShiftIdTwo() {
        return this.shiftIdTwo;
    }

    public List<Visit> getInsertedVisits(int shiftId){
        return insertedVisits.get(shiftId);
    }

    public List<Visit> getAllInsertedVisits() {
        List<Visit> mergedVisits = new ArrayList<>();
        for (Map.Entry<Integer, List<Visit>> entry: insertedVisits.entrySet()){
            mergedVisits.addAll(entry.getValue());
        }
        return mergedVisits;
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
