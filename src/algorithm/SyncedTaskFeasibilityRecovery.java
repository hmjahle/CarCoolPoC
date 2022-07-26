package algorithm;

import java.util.*;
import model.Model;
import model.Visit;
import model.TimeDependentVisitPair;
import model.Shift;
import solution.Problem;
import solution.Solution;
import util.SynchronizedTaskUtils;

public class SyncedTaskFeasibilityRecovery {

    private final Set<Visit> masterVisits;
    private final Map<Visit, Set<TimeDependentVisitPair>> masterToDependent;

    public SyncedTaskFeasibilityRecovery(Model model) {
        masterVisits = new HashSet<>();
        masterToDependent = new HashMap<>();
        initializedMasterTasks(model);
    }

    private void initializedMasterTasks(Model model) {
        for (TimeDependentVisitPair pair : model.getTimeDependentVisitPairs()) {
            Visit masterVisit = pair.getMasterVisit();
            masterVisits.add(masterVisit);
            masterToDependent.putIfAbsent(masterVisit, new HashSet<>());
            masterToDependent.get(masterVisit).add(pair);
        }
    }

    /**
     * Removes all synced tasks where the other task in the pair is unallocated.
     */
    public boolean recoverSyncedFeasibility(Problem problem) {
        Solution solution = problem.getSolution();
        boolean updated = false;

        for (Visit masterVisit : masterVisits) {
            if (shouldUnassignGroup(solution, masterVisit)) {
                unAssignTasksIfUnallocatedTasksAreFound(problem, solution, masterVisit);
                updated = true;
            }
        }
        return updated;
    }

    private boolean shouldUnassignGroup(Solution solution, Visit masterVisit) {
        for (TimeDependentVisitPair pair : masterToDependent.get(masterVisit)) {
            if (SynchronizedTaskUtils.isNotSameAllocationState(solution, pair) || SynchronizedTaskUtils.isStartTimeInvalid(solution, pair)) {
                return true;
            }
        }
        return false;
    }

    private void unAssignTasksIfUnallocatedTasksAreFound(Problem problem, Solution solution, Visit masterVisit) {
        unAssign(problem, solution, masterVisit);
        for (TimeDependentVisitPair pair : masterToDependent.get(masterVisit)) {
            unAssign(problem, solution, pair.getDependentVisit());
        }
    }

    private void unAssign(Problem problem, Solution solution, Visit visit) {
        Shift shift = solution.shiftForVisit(visit);
        if (shift == null) return;
        problem.unAssignVisitFromShift(shift, solution.getRoute(shift).indexOf(visit));
    }
}
    
