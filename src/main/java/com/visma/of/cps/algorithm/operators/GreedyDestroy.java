package com.visma.of.cps.algorithm.operators;

import java.util.Random;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.solution.Problem;

/**
 * The Greedy destroy operator removes a task from a random shift in the solution that reduce the objective value the most.
 * If it does not reduce it, it continues to the next random shift, until no an improvement is found or there are no more
 * shifts.
 */
public class GreedyDestroy extends GreedyDestroyAbstract implements IDestroyOperator {


    public GreedyDestroy(Model model) {
        super(model);
    }

    public GreedyDestroy(Model model, Random random) {
        super(model, random);
    }

    /**
     * Removes the task from a random shift in the solution that reduce the objective value the most.
     * If it does not reduce it, it continues to the next random shift, until no an improvement is found or there are no more
     * shifts.
     * If it is not possible to remove a task from the solution, because it is empty or will render it infeasible
     * it will return null.
     *
     * @param problem Containing original solution to remove the tasks from, must be build from the same
     *                model used to create the operator. (the input solution will be changed).
     * @return NeighborhoodMoveInfo or null.
     */
    @Override
    public NeighborhoodMoveInfo destroy(Problem problem) {
        return internalDestroy(problem);
    }


}