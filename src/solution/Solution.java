package solution;

import model.Model;
import model.Shift;
import model.Task;
import model.Visit;

import java.util.*;

public class Solution {

    private List<List<Visit>> shiftRoutes;
    private Shift[] taskAssignedToShift;
    private Shift[] visitAssignedToShift;
    private Set<Task> unallocatedTasks;
    private Set<Task> allocatedTasks;
    private Set<Visit> unallocatedVisits;

    private int numTasks;

    public Solution(Model model) {
        unallocatedTasks = new HashSet<>();
        unallocatedVisits= new HashSet<>();
        allocatedTasks= new HashSet<>();
        taskAssignedToShift = new Shift[model.getTasks().size()];
        visitAssignedToShift = new Shift[model.getVisits().size()];
        shiftRoutes = new ArrayList<>();
        for (int i = 0; i < model.getShifts().size(); i++) {
            shiftRoutes.add(new ArrayList<>());

        }

    }

    /**
     * Add a visit that is not currently in the solution to a shift and update the affected tata structures
     * @param visit Visit to be inserted
     * @param shift Shift to insert the visit into
     * @param index Position in the route to insert the visit into
     */

    protected void addVisitToShift(Visit visit, Shift shift, int index) {
        setVisitId(visit, shift);
        addVisitToRoute(shift, visit, index);
    }

    protected void assignVisitToShift(Visit visit, Shift shift, int index) {
        unallocatedVisits.remove(visit);
        setVisitId(visit, shift);
        addVisitToRoute(shift, visit, index);
    }

    protected Visit unAssignVisitFromShift(Shift shift, int index) {
        Visit visit = removeFromRoute(shift, index);
        setVisitId(visit, null);
        unallocatedVisits.add(visit);
        return visit;
    }

    /**
     * Add a task that is not currently in the solution, and
     * @param task The task to be inserted into the solution
     */
    protected void addTask(Task task) {
        if (unallocatedTasks.contains(task)) {
            throw new IllegalArgumentException("This task has already been added to the solution");
        }
        unallocatedTasks.add(task);
    }

    protected void allocateTask(Task task) {
        allocatedTasks.add(task);
        unallocatedTasks.remove(task);
    }

    protected void unAllocateTask(Task task) {
        unallocatedTasks.add(task);
        allocatedTasks.remove(task);
    }

    protected boolean isAllocated(Task task) {
        return this.allocatedTasks.contains(task);
    }

    private void setVisitId(Visit visit, Shift shift) {
        visitAssignedToShift[visit.getId()] = shift;
    }

    private void addVisitToRoute(Shift shift, Visit visit, int index) {
        getRoute(shift).add(index, visit);
    }

    protected Visit removeFromRoute(Shift shift, int index) {
        return getRoute(shift).remove(index);
    }

    public List<Visit> getRoute(Shift shift) {
        return getRoute(shift.getShiftId());
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
