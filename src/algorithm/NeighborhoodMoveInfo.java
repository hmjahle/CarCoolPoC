package algorithm;

//import algorithm.Problem;
//import com.visma.of.common.solution.ExtraRouteConstraints;
//import com.visma.of.common.solution.Objective;
// import algorithm.Solution;

/**
 * Contains a solution and the delta objective value representing the change in objective that the changes in
 * neighborhood of the lns has made. Hence the difference from the provided solution to the new version of the solution.
 */
public class NeighborhoodMoveInfo {

    private Problem problem;
    private INeighborhoodMove neighborhoodMove;
    private Double deltaObjectiveValue;

    public NeighborhoodMoveInfo(Problem problem, double deltaObjectiveValue) {
        this.problem = problem;
        this.deltaObjectiveValue = deltaObjectiveValue;
    }

    public NeighborhoodMoveInfo(Problem problem) {
        this.problem = problem;
    }

    public NeighborhoodMoveInfo(INeighborhoodMove neighborhoodMove) {
        this.neighborhoodMove = neighborhoodMove;
    }

    public void setNeighborhoodMove(INeighborhoodMove neighborhoodMove) {
        this.neighborhoodMove = neighborhoodMove;
    }

    public Problem getProblem() {
        return problem;
    }


    public double getDeltaObjectiveValue() {
        return deltaObjectiveValue != null ? deltaObjectiveValue : 0.0;
    }

    public void setDeltaObjectiveValue(double deltaObjectiveValue) {
        this.deltaObjectiveValue = deltaObjectiveValue;
    }

    public INeighborhoodMove getNeighborhoodMove() {
        return neighborhoodMove;
    }

    public boolean possible() {
        return deltaObjectiveValue != null;
    }

/*     public Solution getSolution() {
        return problem.getSolution();
    }

    public Objective getObjective() {
        return problem.getObjective();
    }

    public ExtraRouteConstraints getConstraints() {
        return problem.getConstraints();
    } */

}