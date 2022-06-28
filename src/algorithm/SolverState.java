package algorithm;
import algorithm.Problem;

public class SolverState {
    private final Problem current;
    private final Problem currentFeasible;
    private final Problem bestKnown;
    private final Problem tmpInstance;
    private final Problem candidate;

    private double nextCheckForMovingBetweenInfeasiblePhase;

    public SolverState(Problem problem, double infeasibilitySteps) {
        this.current = new Problem(problem);
        this.currentFeasible = new Problem(problem);
        this.tmpInstance = new Problem(problem);
        this.bestKnown = new Problem(problem);
        this.candidate = new Problem(problem);

        this.nextCheckForMovingBetweenInfeasiblePhase = infeasibilitySteps;
    }

    public Problem getCurrent() {
        return current;
    }

    public Problem getCandidate() {
        return candidate;
    }

    public Problem getCurrentFeasible() {
        return currentFeasible;
    }

    public Problem getBestKnown() {
        return bestKnown;
    }

    public Problem getTmpInstance() {
        return tmpInstance;
    }

    public double getNextCheckForMovingBetweenInfeasiblePhase() {
        return nextCheckForMovingBetweenInfeasiblePhase;
    }

    public void increaseNextCheckForMovingBetweenInfeasiblePhase(double increment) {
        this.nextCheckForMovingBetweenInfeasiblePhase += increment;
    }
    
}
