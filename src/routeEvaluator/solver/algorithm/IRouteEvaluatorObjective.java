package routeEvaluator.solver.algorithm;

/**
 * The objective interface is used to calculate the objective value and check whether one objective dominates the other.
 * It does this by incrementing the objective. Which is for every step in the route, i.e., task to be performed.
 * The objective value is a single double value representing the value of the objective.
 */
public interface IRouteEvaluatorObjective {

    /**
     * Value representing the objective.
     *
     * @return Double.
     */
    double getObjectiveValue();

    /**
     * Return whether an objective dominates another objective, i.e., is better than another in every way.
     *
     * @param other Objective which is compared.
     * @return -1 if this is strictly better, 1 if the other is strictly better or 0 if they are equal or neither dominates.
     */
    int dominates(IRouteEvaluatorObjective other);

    /**
     * Increments the value of the objective providing information about the, objective function, weight and value.
     *
     * @param objectiveFunctionId Id of the objective function.
     * @param weight              Weight of the objective.
     * @param value               Value of the objective.
     */
    void incrementObjective(String objectiveFunctionId, double weight, double value);

    /**
     * Creates a copy of the current objective.
     * @return An IObjective, cannot be null.
     */
    IRouteEvaluatorObjective initializeNewObjective();
}
