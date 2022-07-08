package algorithm.repair;

import algorithm.NeighborhoodMoveInfo;
import model.Shift;
import model.Task;
import model.Visit;

import java.util.List;
import java.util.Set;

public interface IRepairAlgorithm {

    NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Visit> unallocatedTasks);

}
