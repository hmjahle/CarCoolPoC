package algorithm.operators;

import algorithm.NeighborhoodMoveInfo;
import algorithm.Problem;

public interface IDestroyOperator {

    /**
     * Takes an input problem (with solution, objectives and constraints) and performs an operation that
     * removes one or more tasks from a solution.
     * If it is possible it remove one or more tasks from the solution and update the (objectives and constraints)
     * such that it corresponds to the new solution.
     * This is returned within a NeighborhoodMoveInfo where the necessary information about the change in the solution
     * is kept. If the operator does not successfully remove tasks from the solution it returns null.
     *
     * @param problem Solution to be destroyed (the input solution will be changed).
     * @return Null if nothing is removed from the solution otherwise a new NeighborhoodMoveInfo.
     */
    NeighborhoodMoveInfo destroy(Problem problem);
}
