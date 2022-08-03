package com.visma.of.cps.algorithm.operators;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;

public interface IRepairOperator {

    /**
     * Insert unallocated tasks into the solution. If it is not possible to insert a task into the solution, because
     * there is no unallocated tasks or it will render the solution infeasible it will return false otherwise true.
     * The neighborhoodMoveInfo contained in the neighborhood info WILL be altered.
     *
     * @param neighborhoodMoveInfo problem (with solution, objectives and constraints), to insert the task into.
     *                             The solution must be based on the same model used to create the operator. Also
     *                             contains the delta objective value from a previous destroy move that created the
     *                             neighborhood info. This will also be altered by the operator.
     * @return True if successful otherwise false.
     */
    boolean repair(NeighborhoodMoveInfo neighborhoodMoveInfo);
}
