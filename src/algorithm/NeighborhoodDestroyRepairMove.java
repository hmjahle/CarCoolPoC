package algorithm;

import algorithm.operators.IDestroyOperator;
import algorithm.operators.IRepairOperator;
import algorithm.Problem;

public class NeighborhoodDestroyRepairMove implements INeighborhoodMove {

    private IDestroyOperator destroyOperator;
    private IRepairOperator repairOperator;

    NeighborhoodDestroyRepairMove(IDestroyOperator destroyOperator, IRepairOperator repairOperator) {
        this.destroyOperator = destroyOperator;
        this.repairOperator = repairOperator;
    }

    /**
     * Applies a destroy/repair move. There are three basic cases for what can happen when the destroy operator is
     * applied to a solution.
     * 1) the destroy operator succeeds, then proceed to apply the repair operator.
     * 2) if it is not possible to remove anything from the solution (the destroy operator fails) and the solution has
     * no unallocated tasks the operator return null since it is not possible to change the solution.
     * 3) if it is not possible to remove anything from the solution (the destroy operator fails) and the solution has
     * unallocated tasks, proceed to apply the repair operator to insert original tasks into the that the destroy
     * operator could not destroy.
     *
     * @param problem Problem with the solution and its objectives and constraints to be altered (the input solution
     *                will be changed).
     * @return An altered version of the provided solution if both operators are successful or null in destroy case 3 or if the solution cannot
     * be changed at all, i.e., destroy case 2 and the repair operator fails to repair. Then the move return null as the
     * solution would not be altered.
     */
    @Override
    public NeighborhoodMoveInfo applyMove(Problem problem) {
        boolean successfullyDestroyed = false;
        NeighborhoodMoveInfo destroyRepairMoveInfo = destroyOperator.destroy(problem);
        if (destroyRepairMoveInfo == null && !problem.getSolution().getUnallocatedTasks().isEmpty()) {
            destroyRepairMoveInfo = new NeighborhoodMoveInfo(new Problem(problem), 0);
        } else if (destroyRepairMoveInfo == null)
            return null;
        else
            successfullyDestroyed = true;
        if (!repairOperator.repair(destroyRepairMoveInfo) && !successfullyDestroyed)
            return null;

        return destroyRepairMoveInfo;
    }

    public IDestroyOperator getDestroyOperator() {
        return destroyOperator;
    }

    public IRepairOperator getRepairOperator() {
        return repairOperator;
    }

    @Override
    public String toString() {
        return destroyOperator.getClass().getSimpleName() + "->" + repairOperator.getClass().getSimpleName();
    }
}