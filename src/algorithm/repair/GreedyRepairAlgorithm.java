package algorithm.repair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import algorithm.NeighborhoodMoveInfo;
//import com.visma.of.common.FeasibilityChecker;
import model.Model;
import model.Shift;
import model.Task;
import model.TimeDependentVisitPair;
import model.Visit;
import routeEvaluator.results.InsertVisitResult;
import routeEvaluator.results.Route;
import routeEvaluator.results.RouteEvaluatorResult;
import solution.Problem;
import solution.Solution;
import util.CarPoolingTimeDependentPairsUtils;
import util.Constants;
import util.SynchronizedTaskUtils;
import util.Constants.VisitType;
import static util.RandomUtils.objectiveNoise;



public class GreedyRepairAlgorithm implements IRepairAlgorithm {

    protected final Model model;
    private final Random random;
    //private final FeasibilityChecker feasibilityChecker;
    private final CarPoolingTimeDependentPairsUtils carpoolingUtils = new CarPoolingTimeDependentPairsUtils();

    public GreedyRepairAlgorithm(Model model, Random random) {
        this.model = model;
        this.random = random;
        //this.feasibilityChecker = FeasibilityChecker.getInstance();
    }

    /**
     * WARNING: Modifies unallocatedTasks by removing allocated task from it.
     */
    public NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Visit> unallocatedVisits) {
        if (unallocatedVisits.isEmpty()) {
            return null;
        }

        Double repairDelta = findBestGreedyInsert(neighborhoodMoveInfo.getProblem(), shifts, unallocatedVisits);
        if (repairDelta == null) {
            return null;
        }

        neighborhoodMoveInfo.setDeltaObjectiveValue(neighborhoodMoveInfo.getDeltaObjectiveValue() + repairDelta);
        return neighborhoodMoveInfo;
    }

    protected Double findBestGreedyInsert(Problem problem, List<Shift> shifts, Set<Visit> unallocatedVisits) {
        var solution = problem.getSolution();
        var objective = problem.getObjective();
        double bestDeltaObjectiveValue = Double.MAX_VALUE;
        Shift bestShift = null;
        Visit bestVisit = null;
        InsertVisitResult bestRoute = null;

        double deltaObjectiveValue;

        List<Visit> visits = new ArrayList<>(unallocatedVisits);
        Collections.shuffle(visits);
        List<Visit> legalMotorizedVisits = visits.stream().filter(v -> v.completesTask()).collect(Collectors.toList());
        for (Shift shift : shifts) {
            if (shift.isMotorized()){
                for (Visit insertVisit : legalMotorizedVisits){
                    InsertVisitResult result = findRouteForMotorized(problem, insertVisit, solution, shift);
                    if (result.isInfeasibleInsert()) continue;
                    deltaObjectiveValue = result.getDeltaObjective(objective);

                    if (objectiveNoise(random) * deltaObjectiveValue < bestDeltaObjectiveValue) {
                        bestDeltaObjectiveValue = deltaObjectiveValue;
                        bestShift = shift;
                        bestVisit = insertVisit;
                        bestRoute = result;
                    }
                }
            } else {

            }
        }
        if (bestRoute == null) return null;

        Double deltaObjective = updateSolution(problem, bestDeltaObjectiveValue, bestShift, bestVisit, bestRoute);
        unallocatedVisits.remove(bestVisit); // remove unallocated if not already removed

        return deltaObjective;
    }

    private InsertVisitResult findRouteForMotorized(Problem problem, Visit insertVisit, Solution solution, Shift motorizedShift){
        RouteEvaluatorResult resultMotorized = findRoute(problem, insertVisit, solution, motorizedShift);
        int insertIndex = resultMotorized.getRoute().findIndexInRouteVisit(insertVisit);
        Visit predecessor = resultMotorized.getRoute().getVisitSolution().get(insertIndex-1);
        // Inserted a complete task not during carpooling. Case 2: m in PP
        if(insertIndex == 0 || !predecessor.isPickUp()){return new InsertVisitResult(resultMotorized, motorizedShift.getId());}
        // Else we inserted a complete task during a carpooling.
        // Find non motorized carpooling shift ID
        Integer coCarPoolerShiftID = predecessor.getCoCarPoolerShiftID();
        // Find corresponding drop-off, pick-up and JM
        Map<Integer, Visit> correspondingTransportVisits = model.getVisits().stream().filter(v -> v.getTask() == insertVisit.getTask()).collect(Collectors.toMap(Visit::getVisitType, Function.identity()));
        
        // Check if state is correct. Can be removed, because this should never happen. But is nice for debugging
        if (correspondingTransportVisits.size() != 4 || 
            correspondingTransportVisits.containsKey(VisitType.COMPLETE_TASK) ||
            correspondingTransportVisits.containsKey(VisitType.DROP_OF) ||
            correspondingTransportVisits.containsKey(VisitType.PICK_UP) ||
            correspondingTransportVisits.containsKey(VisitType.JOIN_MOTORIZED)) {throw new IllegalStateException("The model do not have 4 visits for each task");}

        Visit pickUp = correspondingTransportVisits.get(VisitType.PICK_UP);
        Visit dropOff = correspondingTransportVisits.get(VisitType.DROP_OF);
        Visit newJM = correspondingTransportVisits.get(VisitType.JOIN_MOTORIZED);
        Visit completeTask = correspondingTransportVisits.get(VisitType.COMPLETE_TASK);
        
        // Insert pick up after complete task. (NB has to be done before insertion of drop off, so that the index is still correct)
        resultMotorized.getRoute().addVisitAtIndex(pickUp, insertIndex+1);
        // Insert drop off behind complete task    
        resultMotorized.getRoute().addVisitAtIndex(dropOff, insertIndex-1);
        insertIndex ++;

        // NB!!! In the following code we change the visits. Should they be copied and not changed directly??

        // Set time windows for D, P and JM
        setTimeWindows(calculateTimeWindos(/*Input all visits etc*/));
        
        // If the coCarPoolerShiftId is not sat, the carpooling is no longer active, but the pick up is still present
        // Case 1.2: m in PP  
        // Will not create time depended pairs or sync them.
        if (coCarPoolerShiftID == null){return new InsertVisitResult(resultMotorized, motorizedShift.getId());}

        // Create time dependent pairs
        List<TimeDependentVisitPair> newTimeDependentVisitPairs = new ArrayList<>();
        Map<Visit, Integer> carpoolSyncedVisitStartTime = new HashMap<>();

        int syncedStartTimeDropOff = carpoolingUtils.getVisitStartTime(resultMotorized.getRoute().getVisitSolution(), dropOff, solution.getSyncedVisitStartTimes(), motorizedShift);
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(dropOff, motorizedShift.getId(), completeTask, motorizedShift.getId(), syncedStartTimeDropOff, 0, carpoolSyncedVisitStartTime));
        int syncedStartTimePickUp = syncedStartTimeDropOff + insertVisit.getVisitDuration();
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(pickUp, motorizedShift.getId(), newJM, motorizedShift.getId(), syncedStartTimePickUp, 0, carpoolSyncedVisitStartTime));

        // Add the corresponding JM to the non motorized route after the predecessor JM 
        Route newNonMotorizedRoute = new Route();
        newNonMotorizedRoute.addVisits(solution.getRoute(coCarPoolerShiftID));
        int indexPredecessorJM = newNonMotorizedRoute.findIndexInRouteVisitTypeTask(predecessor.getTask(), VisitType.JOIN_MOTORIZED);
        newNonMotorizedRoute.addVisitAtIndex(newJM, indexPredecessorJM+1);

        RouteEvaluatorResult resultNonMotorized = getEvaluatorResultByTheOrderOfVisits(newNonMotorizedRoute.getVisitSolution(), problem, motorizedShift);

        return new InsertVisitResult(resultMotorized, resultNonMotorized, newTimeDependentVisitPairs, carpoolSyncedVisitStartTime, motorizedShift.getId(), coCarPoolerShiftID);
    }

    private void setTimeWindows(Map<Visit, List<Integer>> timeWindows){
        for(Map.Entry<Visit, List<Integer>> entry : timeWindows.entrySet()){
            entry.getKey().setStartTime(entry.getValue().get(0));
            entry.getKey().setStartTime(entry.getValue().get(1));
        }
    }


    protected Double updateSolution(Problem problem, double bestObjective, Shift bestShift, Visit bestVisit, RouteEvaluatorResult bestRoute) {
        Integer index = bestRoute.getRoute().findIndexInRouteVisit(bestVisit);
        problem.assignVisitToShiftByIndex(bestShift, bestVisit, index, bestObjective);
        return bestObjective;
    }
    
    /**
     * Insert a list of vistis into a shift and calculate the objective value result. The visit order
     * from the list will be upheld in the resulting route
     * @param visits Visits to be inserted into the shift
     * @param problem The current problem
     * @param shift The shift to insert the visits into
     * @return A route evaluator result. Possibly null if insert is infeasible
     */
    protected RouteEvaluatorResult getEvaluatorResultWithMultipleVisits(List<Visit> visits, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisitsInsertVisits(
                solution.getRoute(shift), visits, solution.getSyncedVisitStartTimes(), shift);
    }

    protected RouteEvaluatorResult getEvaluatorResultByTheOrderOfVisits(List<Visit> visits, Problem problem, Shift shift){
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisits(visits, solution.getSyncedVisitStartTimes(), shift);
    }


    /**
     * This method:
     *  1.  Insert the visit into the shift and runs the route evaluator
     *  2.  Discover which case we have, based on the visits location in the route
     *  3.  Handles potential inserts in other shifts (e.g., Pick-up and drop-off inserts in motorized shifts) 
     * @param problem The current problem
     * @param visit The visit we wish to insert
     * @param solution The current solution
     * @param shift The non-motorized shift we wish to insert the visit into
     * @return A list of route evaluator results with affected routes. Empty list if none is feasible
     */
    private List<RouteEvaluatorResult> findRouteForNonMotorized(Problem problem, Visit visit, Solution solution, Shift shift) {
        List<RouteEvaluatorResult> affectedRoutes = new ArrayList<>();

        RouteEvaluatorResult result = getEvaluatorResult(visit, problem, shift);

        int case = getInsertCaseForNonMotorizedShift(result.getRoute() visit);

    }

    /**
     * This method tries to insert the pick-up and drop-of pair into all motorized shifts, run the route evaluator, and
     * return the best shift
     * @param problem The current problem
     * @param motorizedShifts The possible motorized shifts that can take this transport request
     * @param pickUp 
     * @param dropOf
     * @return Return a route evaluator result with the best shift. Null if no shift is found
     */
    private RouteEvaluatorResult getBestMotorizedShift(Problem problem, List<Shift> motorizedShifts, Visit pickUp, Visit dropOf) {
        RouteEvaluatorResult bestResult = null;
        List<Visit> transportRequest = new ArrayList<>(Arrays.asList(pickUp, dropOf));
        for (Shift mShift : motorizedShifts) {
            RouteEvaluatorResult result = getEvaluatorResultWithMultipleVisits(transportRequest, problem, mShift);

            if (result == null) continue;
            if (bestResult == null) {
                bestResult = result;
                continue;
            }
            if (bestResult.getObjectiveValue() < result.getObjectiveValue()) {
                bestResult = result;
            } 
        } 
        return bestResult;
    }

    protected RouteEvaluatorResult getEvaluatorResult(Visit visit, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisitsInsertVisit(
                solution.getRoute(shift), visit, solution.getSyncedVisitStartTimes(), shift);
    }

    private RouteEvaluatorResult findRoute(Problem problem, Visit visit, Solution solution, Shift shift) {
        return getEvaluatorResult(visit, problem, shift);
    }
}


/*private RouteEvaluatorResult findRoute(Problem problem, Task task, Solution solution, Shift shift) {
 *      Boolean feasible, VisitPair (P1, D1), VisitPair (P2, D2) = insertVisitInShift(Visit, Shift);
 *      if not feasible:
 *           return null 
 *      else if P1, D1, P2, D2 is null:
 *           same logic as before: evaluateRouteByTheOrderOfTasksInsertTask()
 *      else:
 *           Iterate over all drivers shifts:
 *               Test inserting (P1, D2) in driver shift and get delta objective
 *               Test inserting (P2, D2) in driver shift and get delta objective
 *               Test inserting both (P1, D1) and (P2, D2) in driver shift and get delta objective
 *           Choose to insert P1, D1, P2, D2 where the objective is best.
 *           Total change in objective = delta objective for non-motarised worker
 *                                       + delta objective motorized worker 1
 *                                       + delta objective motorized worker 2 
 * }
 * Do updates in all shifts affected by the greedy chosen repair visit 
 */