package com.visma.of.cps.algorithm;

import com.visma.of.cps.solution.Problem;

public interface INeighborhoodMove {

    /**
     * A neighborhood move is one or more operators that alone or in combination can change a solution. The solution
     * provided by the problem will be altered and if this is possible / successful a neighborhood move will
     * return info with the altered solution info (note this is the same object as provided) and a delta objective value
     * representing the change in objective value caused by the move.
     *
     * @param problem Problem (with solution, objectives and constraints)s to perform move on.
     * @return Info on the move if successful, otherwise null.
     */
    default NeighborhoodMoveInfo apply(Problem problem) {
        NeighborhoodMoveInfo neighborhoodMoveInfo = this.applyMove(problem);
        if (neighborhoodMoveInfo == null)
            return new NeighborhoodMoveInfo(this);
        neighborhoodMoveInfo.setNeighborhoodMove(this);
        return neighborhoodMoveInfo;
    }

    NeighborhoodMoveInfo applyMove(Problem problem);


}
