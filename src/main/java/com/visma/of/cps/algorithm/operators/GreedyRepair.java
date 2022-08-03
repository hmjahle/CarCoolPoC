package com.visma.of.cps.algorithm.operators;

import java.util.Random;
import java.util.Set;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;
import com.visma.of.cps.algorithm.repair.GreedyRepairAlgorithm;
import com.visma.of.cps.algorithm.repair.IRepairAlgorithm;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.model.Task;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.solution.Solution;

/**
 * The Greedy insert operator insert the task into the solution in the way that increase the objective value the least.
 */
public class GreedyRepair extends OperatorAbstract implements IRepairOperator {

    private final IRepairAlgorithm repairAlgorithm;
    private final Model model;

    public GreedyRepair(Model model, Random random) {
        super(random);
        this.model = model;
        this.repairAlgorithm = new GreedyRepairAlgorithm(model, random);
    }

    public GreedyRepair(Model model) {
        super(new Random());
        this.model = model;
        this.repairAlgorithm = new GreedyRepairAlgorithm(model, this.random);
    }

    /**
     * Insert an unallocated task into the solution in the way that increase the objective value the least.
     * If it is not possible to insert a task into the solution, because there is no unallocated tasks or it will render
     * the solution infeasible it will return false.
     *
     * @param neighborhoodMoveInfo Contains a problem (with solution, objectives and constraints), to insert the task into.
     *                             The solution must be based on the same model used to create the operator. Also
     *                             contains the delta objective value from a previous destroy move that created the
     *                             neighborhood info. This will also be altered by the operator.
     * @return True if successful otherwise false.
     */
    @Override
    public boolean repair(NeighborhoodMoveInfo neighborhoodMoveInfo) {
        Solution solution = neighborhoodMoveInfo.getSolution();
        Set<Visit> unallocatedVisits = getUnallocatedVisits(solution);
        if (unallocatedVisits.isEmpty()) {
            return false;
        }

        NeighborhoodMoveInfo moveInfo = repairAlgorithm.repair(neighborhoodMoveInfo, model.getShifts(), unallocatedVisits);
        return moveInfo != null;
    }

    private Set<Visit> getUnallocatedVisits(Solution solution) {
        return solution.getUnallocatedVisits();
    }
}
