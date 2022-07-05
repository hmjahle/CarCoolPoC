package algorithm.operators;

import java.util.List;
import java.util.Random;

import algorithm.NeighborhoodMoveInfo;
import model.Model;
import model.Shift;
import model.Task;
import solution.Objective;
import solution.Problem;
import solution.Solution;

public abstract class GreedyDestroyAbstract extends DestroyOperatorAbstract {

    protected Model model;
    protected int[] shiftIndices;

    protected GreedyDestroyAbstract(Model model, Random random) {
        super(random);
        this.model = model;
        if (model == null || model.getShifts().isEmpty()) {
            shiftIndices = new int[0];
        } else {
            shiftIndices = new int[model.getShifts().size()];
            resetRandomShiftFinder();
        }
    }

    private int resetRandomShiftFinder() {
        for (int i = 0; i < shiftIndices.length; i++) {
            shiftIndices[i] = i;
        }
        return shiftIndices.length;
    }

    protected GreedyDestroyAbstract(Model model) {
        this(model, new Random());
    }

    /**
     * Used to find an additional destroy move. The neighborhood move must contain a @NotNull solution and objective. Note that if a new move is found, the
     * provided neighborhood move and the solution and objective contained therein is updated (changed)
     *
     * @param neighborhoodMoveInfo Info on the current move on which to perform further changed. Note that the contained solution can be changed.
     * @return The updated neighborhood move, same as provided. Or null if no change is possible.
     */
    protected NeighborhoodMoveInfo internalDestroy(NeighborhoodMoveInfo neighborhoodMoveInfo) {
        return internalDestroy(neighborhoodMoveInfo, neighborhoodMoveInfo.getProblem());
    }

    /**
     * Used to find a destroy move from a solution. If a move is possible the solution will be altered and a neighborhood move
     * containing the copied solution is returned.
     *
     * @param problem Containing original solution to destroy. The input solution in the problem can be changed.
     * @return New neighborhood move or null if no move is possible.
     */
    protected NeighborhoodMoveInfo internalDestroy(Problem problem) {
        return internalDestroy(null, problem);
    }

    /**
     * Finds a random shift and then removes the task from that shift that reduce the objective value the most.
     * If no improvement is possible it continues to the next shift until there are no more shifts to investigate.
     *
     * @param neighborhoodMoveInfo Can be null, if null and a move is found. Then a new neighborhood move is generated from the solution provided.
     *                             Otherwise it is assumed that this is an additional move to the neighborhood move provided.
     * @param problem              Containing a solution to be used to find the move. If a neighborhoodMoveInfo (that is not null) is provided, this must be the same solution as
     *                             contained inside the neighborhoodMoveInfo.
     * @return A new NeighborhoodMoveInfo if a move is possible, otherwise null.
     */
    private NeighborhoodMoveInfo internalDestroy(NeighborhoodMoveInfo neighborhoodMoveInfo, Problem problem) {
        Solution solution = problem.getSolution();
        Objective objective = problem.getObjective();
        double bestValue = Double.MAX_VALUE;
        double bestIntraObjective = 0.0;
        double bestExtraObjective = 0.0;
        Shift removeShift = null;
        int removeIndex = 0;
        int totalShifts = resetRandomShiftFinder(); // intially length of shif indices
        while (totalShifts != 0) {
            int randInt = random.nextInt(totalShifts);
            int shiftIndex = shiftIndices[randInt];
            Shift shift = model.getShifts().get(shiftIndex);
            totalShifts = updateRandomShifts(totalShifts, randInt, shiftIndex); // Move rand shift to back of (toatal number of shifts-1) shift index list, and move last shift to this index. Returns total shifts--
            List<Task> route = solution.getRoute(shift);
            for (int i = 0; i < route.size(); i++) {
                // OBS!!! This needs to be changed, because if we remove a walking task, we might need to remove p1, d1 and p2,d2
                Double deltaIntraObjective = objective.deltaIntraObjectiveRemovingTaskAtIndex(shift, route, i, solution.getSyncedTaskStartTimes()); // Use route evaluator to see the new objective if task i is removed
                if (skipTask(problem, shift, route, i, deltaIntraObjective)) continue; //The continue statement breaks one iteration
                // We only have intraObjectives for now
                //double deltaExtraObjective = objective.deltaExtraRouteObjectiveValueRemove(shift, route.get(i));
                //double deltaObjective = deltaExtraObjective + deltaIntraObjective;
                double deltaObjective = deltaIntraObjective;
                if (noise(random) * deltaObjective < bestValue) {
                    bestValue = deltaObjective;
                    bestIntraObjective = deltaIntraObjective;
                    //bestExtraObjective = deltaExtraObjective;
                    removeIndex = i;
                    removeShift = shift;
                    // Run check for if any other tasks p1,d p2,d2 must be removed as well
                }
            }
            if (bestValue < 0)
                break;
        }
        if (removeShift != null && (neighborhoodMoveInfo == null || !neighborhoodMoveInfo.possible())) 
        // !neighborhoodMoveInfo.possible() == true if deltaObjectiveValue = null, where delta objective value 
        // represents the change in objective that the changes in neighborhood of the lns has made.
            neighborhoodMoveInfo = new NeighborhoodMoveInfo(problem);
        // bestExtraObjective is always 0.0. Could remove variablle, but want to keep it as similar as possible.
        return update(neighborhoodMoveInfo, bestIntraObjective, bestExtraObjective, removeShift, removeIndex);
    }

    private boolean skipTask(Problem problem, Shift shift, List<Task> route, int i, Double deltaIntraObjective) {
        if (deltaIntraObjective == null || route.get(i).isPrioritized()) // DeltaIntraObjective is Null is the solution is infeasable.
            return true;
        // Calls this to check for the extrarouteconstraints, but we only have intra route
        //return !problem.getConstraints().isFeasibleRemoveTask(shift, route.get(i));
        return false;
    }

    private int updateRandomShifts(int totalShifts, int randInt, int shiftIndex) {
        shiftIndices[randInt] = shiftIndices[totalShifts - 1];
        shiftIndices[totalShifts - 1] = shiftIndex;
        totalShifts--;
        return totalShifts;
    }

    /**
     * If a move is found the neighborhood move is updated (the solution and objective is also changed).
     *
     * @param neighborhoodMoveInfo Neighborhood info to update.
     * @param bestIntraObjective   Intra route objective value for the best move.
     * @param bestExtraObjective   Extra route objective value for the best move.
     * @param removeShift          Shift from which the task should be removed, null if it is not possible to remove an
     * @param removeIndex          Task at this index should be removed from the shift.
     * @return Null if no improvement is found, otherwise the same NeighborhoodMoveInfo as provided.
     */
    private NeighborhoodMoveInfo update(NeighborhoodMoveInfo neighborhoodMoveInfo, double bestIntraObjective,
                                        double bestExtraObjective, Shift removeShift, int removeIndex) {
        if (removeShift != null) {
            neighborhoodMoveInfo.getProblem().unAssignTaskByRouteIndex(removeShift, removeIndex, bestIntraObjective, bestExtraObjective); // updates the objective when the task (remove index) is removed from the shift
            double deltaObjectiveValue = bestIntraObjective + bestExtraObjective + neighborhoodMoveInfo.getDeltaObjectiveValue();
            neighborhoodMoveInfo.setDeltaObjectiveValue(deltaObjectiveValue);
            return neighborhoodMoveInfo;
        } else
            return null;
    }

}