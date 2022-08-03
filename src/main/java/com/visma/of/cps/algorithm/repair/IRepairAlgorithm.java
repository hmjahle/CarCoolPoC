package com.visma.of.cps.algorithm.repair;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;

import java.util.List;
import java.util.Set;

public interface IRepairAlgorithm {

    NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Visit> unallocatedTasks);

}
