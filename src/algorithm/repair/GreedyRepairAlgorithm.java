package algorithm.repair;

import java.util.*;

import algorithm.NeighborhoodMoveInfo;
//import com.visma.of.common.FeasibilityChecker;
import model.Model;
import model.Shift;
import model.Task;
import model.Visit;
import routeEvaluator.results.RouteEvaluatorResult;
import solution.Problem;
import solution.Solution;


public class GreedyRepairAlgorithm implements IRepairAlgorithm {

    protected final Model model;
    private final Random random;
    //private final FeasibilityChecker feasibilityChecker;

    public GreedyRepairAlgorithm(Model model, Random random) {
        this.model = model;
        this.random = random;
        //this.feasibilityChecker = FeasibilityChecker.getInstance();
    }

    /**
     * WARNING: Modifies unallocatedTasks by removing allocated task from it.
     */
    public NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Visit> unallocatedTasks) {
        if (unallocatedTasks.isEmpty()) {
            return null;
        }

        Double repairDelta = findBestGreedyInsert(neighborhoodMoveInfo.getProblem(), shifts, unallocatedTasks);
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
        RouteEvaluatorResult bestRoute = null;

        double deltaIntraObjectiveValue;
        double deltaExtraObjectiveValue;
        double deltaObjectiveValue;

        List<Visit> visits = new ArrayList<>(unallocatedVisits);
        Collections.shuffle(visits);

        for (Shift shift : shifts) {
            if (shift.isMotorized()){
                for (Visit insertVisit : visits){
                    
                }
            } else {

            }
            for (Visit insertVisit : visits) {
                RouteEvaluatorResult result = findRoute(problem, insertVisit, solution, shift);
                if (result == null) continue;
                
                /* deltaIntraObjectiveValue1 = result.getObjectiveValue() - objective.getShiftIntraRouteObjectiveValue(shift);
                deltaIntraObjectiveValue2 = result.getObjectiveValue() - objective.getShiftIntraRouteObjectiveValue(shift);
                deltaIntraObjectiveValue3 = result.getObjectiveValue() - objective.getShiftIntraRouteObjectiveValue(shift);
                totalDelta = deltaIntraObjectiveValue1 + deltaIntraObjectiveValue2 + deltaIntraObjectiveValue3;

                //deltaExtraObjectiveValue = objective.calcDeltaExtraRouteObjectiveValueInsert(shift, insertTask);
                //deltaObjectiveValue = deltaIntraObjectiveValue + deltaExtraObjectiveValue;

                if (objectiveNoise(random) * deltaObjectiveValue < bestDeltaObjectiveValue) {
                    bestDeltaObjectiveValue = deltaObjectiveValue;
                    bestIntraObjectiveValue = deltaIntraObjectiveValue;
                    bestExtraObjectiveValue = deltaExtraObjectiveValue;
                    bestShift = shift;
                    bestVisit = insertVisit;
                    bestRoute = result;
                }*/
            }
        }
        if (bestRoute == null) return null;

        Double deltaObjective = updateSolution(problem, bestIntraObjectiveValue, bestExtraObjectiveValue, bestShift, bestVisit, bestRoute);
        unallocatedVisits.remove(bestVisit); // remove unallocated if not already removed

        return deltaObjective;
    }

    protected Double updateSolution(Problem problem, double bestIntraObjective, double bestExtraObjective, Shift bestShift, Visit bestVisit, RouteEvaluatorResult bestRoute) {
        Integer index = bestRoute.getRoute().findIndexInRouteVisit(bestVisit);
        problem.assignVisitToShiftByIndex(bestShift, bestVisit, index, bestIntraObjective);
        return bestIntraObjective + bestExtraObjective;
    }

    protected RouteEvaluatorResult getEvaluatorResult(Visit visit, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisitsInsertVisit(
                solution.getRoute(shift), visit, solution.getSyncedVisitStartTimes(), shift);
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

    private List<RouteEvaluatorResult> findRoute(Problem problem, Visit visit, Solution solution, Shift shift) {
        // We have no extra route constraint so we can just delegate 
        if (shift.isMotorized()) {
            return findRouteForMotorized(problem, visit, solution, shift);
        }
        return findRouteForNonMotorized(problem, visit, solution, shift); 
    }

    private List<RouteEvaluatorResult> findRouteForMotorized(Problem problem, Visit visit, Solution solution, Shift shift) {
        // ToDo: implement
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
}

private int getInsertCaseForNonMotorizedShift(Route route, Visit visit) {

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