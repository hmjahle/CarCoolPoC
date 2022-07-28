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
        double bestIntraObjectiveValue = 0.0;
        double bestExtraObjectiveValue = 0.0;
        Shift bestShift = null;
        Visit bestVisit = null;
        RouteEvaluatorResult bestRoute = null;

        double deltaIntraObjectiveValue;
        double deltaExtraObjectiveValue;
        double deltaObjectiveValue;

        List<Visit> visits = new ArrayList<>(unallocatedVisits);
        Collections.shuffle(visits);

        for (Shift shift : shifts) {
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

    private RouteEvaluatorResult findRoute(Problem problem, Visit visit, Solution solution, Shift shift) {

        // Must return true, and if true you must insert task p and d as well to one or two drivers
        // Feasibilty must return feasible (boolean), (possible p1 d1) (possible p2 d2)
        // must check if task can be inserted
        // If yes, but you must insert p1, d1 and p2, d2 in 1/2 drives:
            // Iterate over all drivers shift
                // feasibility check for p1, d1 for driver after T_i1,j1 
                    // if feasible: Get objective
                    // check if it is better
                //feasibilty check p2,d2 for driver after Ti2,j2
                    // if feasible: Get objective
                    // check if it is better
                //feasibilty check for both insert p1, d1 and p2,d2 for driver after Ti2,j2
            // choose which shift??? 
                // algorithm 3 in paper
            // run route evaluter for all three (possibly two) shifts.
            

        // else do as before

        // Feasibility checker is only for syncronized taskes, so we dont need to use it
        /* if (feasibilityChecker.canNotBeInsertedInRoute(model, problem.getConstraints(), solution, shift, task)) {
            return null;
        } */
        // Add the extra tasks as well
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