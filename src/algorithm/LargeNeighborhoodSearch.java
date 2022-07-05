package algorithm;
import java.util.IntSummaryStatistics;

import algorithm.heuristics.SimulatedAnnealing;
import algorithm.operators.GreedyDestroy;
import algorithm.operators.GreedyRepair;
import model.Model;
import model.Task;
import solution.Problem;
import solution.Solution;


public class LargeNeighborhoodSearch {
    private static final double INFEASIBILITY_STEPS = 0.01;
    public static final long MINIMUM_MILLISECONDS_BETWEEN_CONVERGENCE_IS_CHECKED_DEFAULT = 30 * 1000L;
    private static final double WORSE_SOLUTION_ACCEPTED_AT_PROBABILITY = 0.5;
    private static final double PROPORTION_OF_RUNTIME_USED_FOR_DEEP_DIVE = 0.2;
    private static final boolean UNALLOCATED_TASKS_ARE_HIERARCHICAL_OBJECTIVE = true;
    private static final double MINIMUM_IMPROVEMENT_PERCENTAGE_TO_BE_SIGNIFICANT = 0.0005;



    private Model model;
    private long minimumMillisecondsBetweenConvergenceIsChecked;
    private long lastConvergenceCheck;
    private boolean hasLikelyConverged;
    private NeighborhoodSelector neighborhoodSelector;
    private SimulatedAnnealing simulatedAnnealing;
    private boolean unallocatedTasksAreHierarchical = UNALLOCATED_TASKS_ARE_HIERARCHICAL_OBJECTIVE;




    public LargeNeighborhoodSearch(Model model){
       intialize(model);
    }


    private void intialize(Model mode){
        this.model = model;
        this.neighborhoodSelector = new NeighborhoodSelector();
        this.minimumMillisecondsBetweenConvergenceIsChecked = MINIMUM_MILLISECONDS_BETWEEN_CONVERGENCE_IS_CHECKED_DEFAULT;
        this.simulatedAnnealing = new SimulatedAnnealing(WORSE_SOLUTION_ACCEPTED_AT_PROBABILITY);
        initializeSimulatedAnnealingCriteriaRuntime(model);
    }

    public void useIterationsAsStopCriteria(int iterations) {
        this.simulatedAnnealing = new SimulatedAnnealing(WORSE_SOLUTION_ACCEPTED_AT_PROBABILITY);
        this.simulatedAnnealing.setIterationsStopCriteria(iterations, iterations, iterations / 10);
    }


    private void initializeSimulatedAnnealingCriteriaRuntime(Model model) {
        initializeSimulatedAnnealingCriteriaRuntime(model.getConfiguration().getSolverRuntime(), PROPORTION_OF_RUNTIME_USED_FOR_DEEP_DIVE);
    }

    private void initializeSimulatedAnnealingCriteriaRuntime(double runtime, double runtimeDeepDive) {
        this.simulatedAnnealing.setTimeStopCriteria((long) (runtime * 1000),
                (long) (runtime * 1000 * (1 - runtimeDeepDive)),
                (long) (runtime * 1000 * runtimeDeepDive));
    }

        /**
     * Initialize and add the standard destroy, repair and improve operators to the lns algorithm.
     */
    public void initializeStandardOperators() {
        neighborhoodSelector.addNeighborhood(new GreedyDestroy(model));
        neighborhoodSelector.addNeighborhood(new GreedyRepair(model));
    }

    public Problem solveWithConstructionHeuristic(Problem problem){
        /*
         * Here in orginal code, the problem is first solved by adjusting the 
         * objective weight of travel time to dominate all other objectives. 
         * Then it is solved with modified configurations.
         * We do not use modified configurations, so we can just call solve. 
        */
        return solve(problem);

    }

    public Problem solve(Problem problem) {
        var solverState = new SolverState(problem, INFEASIBILITY_STEPS);
        simulatedAnnealing.startSearch();
        lastConvergenceCheck = simulatedAnnealing.getCurrentRuntime();
        while (simulatedAnnealing.continueSearch()) {
            var neighborhoodMoveInfo = neighborhoodSelector.applyRandomNeighborhood(solverState.getTmpInstance());

            updateSolverState(solverState, neighborhoodMoveInfo);
        }
        return solverState.getBestKnown();
    }


    // Before also input feasibility checker
    private void updateSolverState(SolverState solverState, NeighborhoodMoveInfo neighborhoodMoveInfo) {
        if (neighborhoodMoveInfo.possible() && acceptNewSolution(solverState, neighborhoodMoveInfo)) {
            neighborhoodSelector.acceptMove(neighborhoodMoveInfo);
            updateSolutions(solverState, neighborhoodMoveInfo.getProblem());
        } else {
            neighborhoodSelector.rejectMove(neighborhoodMoveInfo);
            solverState.getTmpInstance().update(solverState.getCurrent());
        }
    }

    double calculateMainWeightRatio(double mainObjectiveWeight) {
        return mainObjectiveWeight >= 0.9 ? 1 : 0.9 / mainObjectiveWeight;
    }

    double calculateSubWeightRatio(double mainObjectiveWeight) {
        return mainObjectiveWeight >= 0.9 ? 1 : .1 / (1 - mainObjectiveWeight);
    }

        /**
     * A current problem is being evaluated to see if it improves the current best problem. If in an infeasible phase
     * the feasibility of the current problem has to be checked.
     *
     * @param solverState Current solver state holding infeasibility status, checks and working problems
     * @param newProblem  The new problem from the neighborhood move, that is already accepted.
     */
    private void updateSolutions(SolverState solverState, Problem newProblem) {
        solverState.getCurrent().update(newProblem);

        if (updateBestKnownSolution(solverState.getCurrentFeasible(), solverState.getBestKnown())) {
            if ((simulatedAnnealing.getCurrentRuntime() - lastConvergenceCheck) > minimumMillisecondsBetweenConvergenceIsChecked) {
                hasLikelyConverged = true;
            }
        } else {
            lastConvergenceCheck = simulatedAnnealing.getCurrentRuntime();
        }
    }





    /**
     * Check whether a new solution should be accepted or not. Does not check for feasibility, just objectives.
     *
     * @param solverState          current solver state
     * @param neighborhoodMoveInfo New problem to be tested if it can be accepted.
     * @return If the new problem should be accepted.
     */
    private boolean acceptNewSolution(SolverState solverState, NeighborhoodMoveInfo neighborhoodMoveInfo) {
        int newSolutionDominance = dominatesUnallocatedTasks(solverState.getCurrent().getSolution(), neighborhoodMoveInfo.getSolution());

        if (unallocatedTasksAreHierarchical && newSolutionDominance < 0) {
            return true;
        }

        return newSolutionDominance <= 0 && simulatedAnnealing.acceptSolution(solverState.getCurrent().getObjective().getTotalObjectiveValue(), neighborhoodMoveInfo.getDeltaObjectiveValue());
    }

    /**
     * Check if the current solution dominates another solution on unallocated tasks. First the sum of the duration of
     * the unallocated tasks are compared. Here it is preferred to have as little time as possible allocated.  If this
     * is tied then if it has less unallocated tasks it dominates.
     *
     * @param current       Current solution to compare to new solution.
     * @param otherSolution Solution being compared.
     * @return Integer, -1, 0 or 1. Depending on whether the new dominates the old (-1), they are equal (0) and otherwise 1.
     */
    private static int dominatesUnallocatedTasks(Solution current, Solution otherSolution) {
        IntSummaryStatistics currentStats = current.getUnallocatedTasks().stream().mapToInt(Task::getDuration).summaryStatistics();
        IntSummaryStatistics otherStats = otherSolution.getUnallocatedTasks().stream().mapToInt(Task::getDuration).summaryStatistics();

        int compare = Long.compare(otherStats.getSum(), currentStats.getSum());
        return compare == 0 ? Long.compare(otherStats.getMax(), currentStats.getMax()) : compare;
    }


    /**
     * Updates best know solution with the newSolution.
     * Reports if the objective change is significant for the solution to have likely converged.
     *
     * @param newSolution Problem with newSolution solution, which is a potential new best solution.
     * @param bestKnown   Problem with best known solution thus far. To be updated to the newSolution if found to be better.
     * @return True if the solution has likely converged.
     */
    public static boolean updateBestKnownSolution(Problem newSolution, Problem bestKnown) {
        int newSolutionDominance = dominatesUnallocatedTasks(bestKnown.getSolution(), newSolution.getSolution());

        if (newSolutionDominance < 0) {
            bestKnown.update(newSolution);
            return false;
        }

        double bestKnownObjectiveValue = bestKnown.getObjective().getTotalObjectiveValue();
        double newSolutionObjectiveValue = newSolution.getObjective().getTotalObjectiveValue();

        if (newSolutionDominance == 0 && newSolutionObjectiveValue < bestKnownObjectiveValue) {
            bestKnown.update(newSolution);
            return convergenceIsLikely(newSolutionObjectiveValue, bestKnownObjectiveValue);
        }

        return true;
    }

    /**
     * Check if it is likely that the solver is converging.
     *
     * @param newObjectiveValue       New objective value being compared to the best known objective.
     * @param bestKnownObjectiveValue Current best value to test towards.
     * @return True if convergence is likely, otherwise false.
     */
    private static boolean convergenceIsLikely(double newObjectiveValue, double bestKnownObjectiveValue) {
        double improvement = bestKnownObjectiveValue - newObjectiveValue;
        double percentageImprovement = improvement / bestKnownObjectiveValue;
        return percentageImprovement < MINIMUM_IMPROVEMENT_PERCENTAGE_TO_BE_SIGNIFICANT;
    }

    public SimulatedAnnealing getSimulatedAnnealing() {
        return simulatedAnnealing;
    }

    public NeighborhoodSelector getNeighborhoodSelector() {
        return neighborhoodSelector;
    }

    public Model getModel() {
        return model;
    }

    public void setUnallocatedTasksAreHierarchical(boolean unallocatedTasksAreHierarchical) {
        this.unallocatedTasksAreHierarchical = unallocatedTasksAreHierarchical;
    }

    public boolean getHasLikelyConverged() {
        return hasLikelyConverged;
    }

    public void setMinimumMillisecondsBetweenConvergenceIsChecked(long minimumMillisecondsBetweenConvergenceIsChecked) {
        this.minimumMillisecondsBetweenConvergenceIsChecked = minimumMillisecondsBetweenConvergenceIsChecked;
    }


}
