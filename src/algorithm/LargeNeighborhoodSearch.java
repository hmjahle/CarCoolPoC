package algorithm;
import model.Model;
import algorithm.Problem;

public class LargeNeighborhoodSearch {
    private static final double INFEASIBILITY_STEPS = 0.01;
    public static final long MINIMUM_MILLISECONDS_BETWEEN_CONVERGENCE_IS_CHECKED_DEFAULT = 30 * 1000L;
    private static final double WORSE_SOLUTION_ACCEPTED_AT_PROBABILITY = 0.5;


    private Model model;
    private long minimumMillisecondsBetweenConvergenceIsChecked;
    private long lastConvergenceCheck;
    private SimulatedAnnealing simulatedAnnealing;



    public LargeNeighborhoodSearch(Model model){
       intialize(model);
    }
    private void intialize(Model mode){
        this.model = model;
        this.simulatedAnnealing = new SimulatedAnnealing(WORSE_SOLUTION_ACCEPTED_AT_PROBABILITY);
        this.minimumMillisecondsBetweenConvergenceIsChecked = MINIMUM_MILLISECONDS_BETWEEN_CONVERGENCE_IS_CHECKED_DEFAULT;
        this.neighborhoodSelector = new NeighborhoodSelector();
    }
    public Problem solveWithConstructionHeuristic(Problem problem){
        /*
         * Here in orginal code, the problem is first solved by adjusting the 
         * objective weight of travel time to dominate all other objectives. 
        */
        return solve(problem);

    }

    public Problem solve(Problem problem) {
        var solverState = new SolverState(problem, INFEASIBILITY_STEPS);
        simulatedAnnealing.startSearch();
        lastConvergenceCheck = simulatedAnnealing.getCurrentRuntime();
        while (simulatedAnnealing.continueSearch() && isNotTerminated()) {
            /* Necessary for syncronised tasks 
            if (simulatedAnnealing.percentageDone() > solverState.getNextCheckForMovingBetweenInfeasiblePhase()) {
                updateInfeasibilityPhase(solverState, feasibilityCheck);
            } */
            var neighborhoodMoveInfo = neighborhoodSelector.applyRandomNeighborhood(solverState.getTmpInstance());

            updateSolverState(solverState, feasibilityCheck, neighborhoodMoveInfo);
            reportBestSolution(solverState, false);
        }
        reportBestSolution(solverState, true); // report after solving
        return solverState.getBestKnown();
    }
}
