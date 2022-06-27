package algorithm.repair;

import algorithm.NeighborhoodMoveInfo;
//import com.visma.of.common.FeasibilityChecker;
import model.Model;
import model.Shift;
import model.Task;
import Problem;
import Solution;
import com.visma.of.rp.routeevaluator.results.RouteEvaluatorResult;

import java.util.*;

import static com.visma.of.common.util.RandomUtils.objectiveNoise;


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
    public NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Task> unallocatedTasks) {
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

    protected Double findBestGreedyInsert(Problem problem, List<Shift> shifts, Set<Task> unallocatedTasks) {
        var solution = problem.getSolution();
        var objective = problem.getObjective();
        double bestDeltaObjectiveValue = Double.MAX_VALUE;
        double bestIntraObjectiveValue = 0.0;
        double bestExtraObjectiveValue = 0.0;
        Shift bestShift = null;
        Task bestTask = null;
        RouteEvaluatorResult<Task> bestRoute = null;

        double deltaIntraObjectiveValue;
        double deltaExtraObjectiveValue;
        double deltaObjectiveValue;

        List<Task> tasks = new ArrayList<>(unallocatedTasks);
        Collections.shuffle(tasks);

        for (Shift shift : shifts) {
            for (Task insertTask : tasks) {
                RouteEvaluatorResult<Task> result = findRoute(problem, insertTask, solution, shift);
                if (result == null) continue;

                deltaIntraObjectiveValue1 = result.getObjectiveValue() - objective.getShiftIntraRouteObjectiveValue(shift);
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
                    bestTask = insertTask;
                    bestRoute = result;
                }
            }
        }
        if (bestRoute == null) return null;

        Double deltaObjective = updateSolution(problem, bestIntraObjectiveValue, bestExtraObjectiveValue, bestShift, bestTask, bestRoute);
        unallocatedTasks.remove(bestTask); // remove unallocated if not already removed

        return deltaObjective;
    }

    protected Double updateSolution(Problem problem, double bestIntraObjective, double bestExtraObjective, Shift bestShift, Task bestTask, RouteEvaluatorResult<Task> bestRoute) {
        Integer index = bestRoute.getRoute().findIndexInRoute(bestTask);
        problem.assignTaskToShiftByIndex(bestShift, bestTask, index, bestIntraObjective, bestExtraObjective);
        return bestIntraObjective + bestExtraObjective;
    }

    protected RouteEvaluatorResult<Task> getEvaluatorResult(Task task, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfTasksInsertTask(
                solution.getRoute(shift), task, solution.getSyncedTaskStartTimes(), shift);
    }

    private RouteEvaluatorResult<Task> findRoute(Problem problem, Task task, Solution solution, Shift shift) {

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
        return getEvaluatorResult(task, problem, shift);
    }
}
