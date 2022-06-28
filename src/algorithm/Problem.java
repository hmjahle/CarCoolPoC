package algorithm;

import model.Model;
import model.Shift;
import model.Visit;
import util.Constants.*;
import java.util.Set;

public class Problem {

    private Solution solution;
    private Objective objective;

    public Problem(Model model) {
        this.solution = new Solution(model);
        this.objective = new Objective(model);
    }

    public Set<Visit> getFeasibleUnallocatedVisits() {
        return null;
    }

    public void assignTaskToShiftByIndex(Shift shift, Visit visit, int index, double intraObjectiveDeltaValue) {
        solution.addVisitToShift(visit, shift, index);
        if (visit.getTransportType() == TransportMode.WALK) {
            // This means you completed the task of the previous visit
            solution.completeTaskIfPossible(shift, index - 1);
        }

    }



}
