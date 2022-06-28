package algorithm;

import model.Model;
import model.Shift;
import model.Task;
import model.Visit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {

    private List<List<Visit>> shiftRoutes;
    private Shift[] taskAssignedToShift;
    private Shift[] visitAssignedToShift;
    private Set<Task> unallocatedTasks;
    private Set<Visit> unallocatedVisits;

    private int numTasks;

    public Solution(Model model) {
        unallocatedTasks = new HashSet<>();
        unallocatedVisits= new HashSet<>();
        shiftRoutes = new ArrayList<>();
        for (int i = 0; i < model.getShifts().size(); i++) {
            shiftRoutes.add(new ArrayList<>());

        }
    }

    protected void addVisitToShift(Shift shift, Visit visit, int index) {
        setTaskId(visit, shift);
        addTaskToRoute(shift, visit, index);
    }

    protected Visit removeVisitFromShift(Shift shift, Visit removedVisit) {
        removeFromRoute(shift, removedVisit);
        setTaskId(removedVisit, null);
        return removedVisit;
    }

    private void setTaskId(Visit visit, Shift shift) {
        taskAssignedToShift[visit.getId()] = shift;
    }

    private void addTaskToRoute(Shift shift, Visit visit, int index) {
        getRoute(shift).add(index, visit);
    }

    protected boolean removeFromRoute(Shift shift, Visit visit) {
        return getRoute(shift).remove(visit);
    }

    public List<Visit> getRoute(Shift shift) {
        return getRoute(shift.getId());
    }

    public List<Visit> getRoute(int shiftid) {
        return shiftRoutes.get(shiftid);
    }

    @Override
    public String toString() {
        return "This is a solution";
    }

    public static void main(String[] args) {
        Model model = new Model(4);
        model.loadData();
        Solution solution = new Solution(model);
        System.out.println(solution.toString());
    }

}
