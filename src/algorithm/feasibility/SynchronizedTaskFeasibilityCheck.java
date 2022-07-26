package algorithm.feasibility;

import model.Model;
import model.TimeDependentVisitPair;
import algorithm.SolverState;
import solution.Problem;
import solution.Solution;
import util.SynchronizedTaskUtils;

public class SynchronizedTaskFeasibilityCheck {

    private boolean isInInfeasibilityPhase;
    private final Model model;

    public SynchronizedTaskFeasibilityCheck(Model model, boolean isInInfeasibilityPhase) {
        this.isInInfeasibilityPhase = isInInfeasibilityPhase;
        this.model = model;
    }

    /**
     * Activate infeasibility phase by allowing state where only one of the tasks from synchronized pair is
     * allocated.
     */
    public boolean activateInfeasibilityPhase(SolverState solverState) {
        isInInfeasibilityPhase = true;
        return true;
    }

    /**
     * Deactivates infeasibility phase by enforcing that tasks of synchronized tasks both should be allocated or unallocated.
     */
    public void deactivateInfeasibilityPhase(SolverState solverState) {
        if (isInInfeasibilityPhase) {
            Problem current = solverState.getCurrent();
            isInInfeasibilityPhase = false;

            if (!isSyncTaskAllocationFeasible(current.getSolution())) {
                current.update(solverState.getCurrentFeasible());
            }
            solverState.getTmpInstance().update(current);
        }
    }

    public boolean isFeasibleInPhase(SolverState state) {
        return isInInfeasibilityPhase;
    }

    public boolean isFeasible(SolverState state, Problem currentFeasibleCandidate) {
        return isSyncTaskAllocationFeasible(currentFeasibleCandidate.getSolution());
    }

    private boolean isSyncTaskAllocationFeasible(Solution solution) {
        for (TimeDependentVisitPair pair : model.getTimeDependentVisitPairs()) {
            if (SynchronizedTaskUtils.isNotSameAllocationState(solution, pair) || SynchronizedTaskUtils.isStartTimeInvalid(solution, pair)) {
                return false;
            }
        }
        return true;
    }

    public boolean isInfeasibilityActive() {
        return isInInfeasibilityPhase;
    }
}

