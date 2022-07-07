package solution;

import model.Model;
import model.Shift;
import model.Task;
import model.Visit;
import util.Constants.TransportMode;

import java.util.List;
import java.util.Set;

public class Problem {

    private Solution solution;
    private Objective objective;

    public Problem(Model model) {
        this.solution = new Solution(model);
        this.objective = new Objective(model);
    }

    /**
     * Assigns a visit to a shift at a given index and updates the necessary data structures. Also checks whether
     * this visit-insertion allocates any tasks.
     * @param shift
     * @param visit
     * @param index
     * @param intraObjectiveDeltaValue
     */
    public void assignVisitToShiftByIndex(Shift shift, Visit visit, int index, double intraObjectiveDeltaValue) {
        solution.assignVisitToShift(visit, shift, index);

        // Checks whether the insertion of this visit completes any tasks

        List<Visit> route = solution.getRoute(shift);

        if (visit.getTransportType() == TransportMode.WALK && index > 0) {
            // You completed the task on the previous index
            Task predecessor = route.get(index - 1).getTask();
            solution.allocateTask(predecessor);
        }

        if (index < route.size() - 1) {
            // Visit has a successor
            Visit successor = route.get(index + 1);
            if (successor.getTransportType() == TransportMode.WALK) {
                // Successor walks, i.e., completes the task of the visit that is to be inserted
                solution.allocateTask(successor.getTask());
            }
        }

        // ToDo: update objective function

        // ToDo: update constraints

    }

    /**
     * Un assigns a visit from a shift on a given index and un assigns a task if the successor of this visit cannot
     * take over the task
     * @param shift The shift to remove the visit from
     * @param index The index on the shift-route to remove the visit
     * @param intraObjectiveDeltaValue
     */
    public void unAssignVisitByRouteIndex(Shift shift, int index, double intraObjectiveDeltaValue) {
        List<Visit> route = solution.getRoute(shift);
        Visit removedVisit = solution.unAssignVisitFromShift(shift, index);

        if (0 < index && index <= route.size()) {
            Visit successor = route.get(index);
            Visit predecessor = route.get(index-1);
            if (removedVisit.getTransportType() == TransportMode.WALK
                    && successor.getTransportType() != TransportMode.WALK) {
                // We removed a walking visit and the successor cannot "take over", thus we need to un allocate the
                // task of the predecessor
                solution.unAllocateTask(predecessor.getTask());
            }
        }

        // ToDo: update objective function

        // ToDo: update constraints
    }

}
