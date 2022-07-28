package solution;

import model.Model;
import model.Shift;
import model.Task;
import model.Visit;
import model.TimeDependentVisitPair;

import java.util.*;

public class Solution {

    protected List<List<Visit>> shiftRoutes;
    private Shift[] taskAssignedToShift;
    private Shift[] visitAssignedToShift;
    private Set<Task> unallocatedTasks;
    private Set<Task> allocatedTasks;
    private Map<Visit, Integer> timeDependentVisitStartTime;
    private Collection<TimeDependentVisitPair> carpoolTimeDependentVisitPairs = new HashSet<>();
    private Map<Visit, Integer> carpoolTimeDependentVisitStartTime;
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
        timeDependentVisitStartTime = new HashMap<>();
        for (TimeDependentVisitPair timeDependentVisitPair : model.getTimeDependentVisitPairs()) {
            timeDependentVisitStartTime.put(timeDependentVisitPair.getMasterVisit(), timeDependentVisitPair.getMasterVisit().getStartTime());
            timeDependentVisitStartTime.put(timeDependentVisitPair.getDependentVisit(), Math.max(timeDependentVisitPair.getDependentVisit().getStartTime(), timeDependentVisitPair.getMasterVisit().getStartTime() + timeDependentVisitPair.getIntervalStart()));
        }
    }

    public Solution(Solution other) {
        this.shiftRoutes = new ArrayList<>(other.shiftRoutes.size());
        for (List<Visit> route : other.shiftRoutes)
            this.shiftRoutes.add(new ArrayList<>(route));
        this.taskAssignedToShift = Arrays.copyOf(other.taskAssignedToShift, other.taskAssignedToShift.length);
        this.unallocatedTasks = new HashSet<>(other.unallocatedTasks);
        this.timeDependentVisitStartTime = new HashMap<>(other.timeDependentVisitStartTime);
    }

    protected void update(Solution other) {
        for (int i = 0; i < other.shiftRoutes.size(); i++) {
            this.shiftRoutes.set(i, new ArrayList<>(other.shiftRoutes.get(i)));
        }
        System.arraycopy(other.taskAssignedToShift, 0, this.taskAssignedToShift,
                0, other.taskAssignedToShift.length);
        this.unallocatedTasks.clear();
        this.unallocatedTasks.addAll(other.unallocatedTasks);
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
        visit.resetVisitWhenRemovedFromShift();
        unallocatedVisits.add(visit);
        return visit;
    }

    protected Visit unAssignVisitFromShift(int shiftId, int index) {
        Visit visit = removeFromRoute(shiftId, index);
        setVisitId(visit, null);
        visit.resetVisitWhenRemovedFromShift();
        unallocatedVisits.add(visit);
        return visit;
    }

    public List<Visit> unAssignVisitsConnectedToTask(int shiftId, Visit visit){
        List<Visit> removedVisits = new ArrayList<>();
        int i = 0;
        for(Visit v : getRoute(shiftId)){
            if(v.getTask() == visit.getTask()){
                removedVisits.add(unAssignVisitFromShift(shiftId, i));
            }
            i++;
        }
        return removedVisits;
    }

    public List<Integer> getTransportVisitIndices(int shiftId, Visit visit){
        List<Integer> transportVisitIndices = new ArrayList<>();
        int i = 0;
        for(Visit v : getRoute(shiftId)){
            if(!v.completesTask() && v.getTask() == visit.getTask()){
                transportVisitIndices.add(i);
            }
            i++;
        }
        return transportVisitIndices;
    }

    public Shift shiftForVisit(Visit visit) { return shiftForVisit(visit.getId());}

    public Shift shiftForVisit(int visitId) { return visitAssignedToShift[visitId];}

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

    public boolean isAllocated(Task task) {
        return this.allocatedTasks.contains(task);
    }

    public boolean isVisitAllocated(Visit visit){
        return !(this.unallocatedVisits.contains(visit));
    }

    private void setVisitId(Visit visit, Shift shift) {
        visitAssignedToShift[visit.getId()] = shift;
    }

    private void addVisitToRoute(Shift shift, Visit visit, int index) {
        getRoute(shift).add(index, visit);
    }

    // Method to add new carpoolTimeDependentVisitPair, because this list is initially empty. Input: Visits
    protected void addCarpoolTimeDependentVisitPair(Visit master, Visit dependent, int intervalStart, int intervalEnd){
        TimeDependentVisitPair carpoolPair = new TimeDependentVisitPair(master, dependent, intervalStart, intervalEnd);
        this.carpoolTimeDependentVisitPairs.add(carpoolPair);
        this.carpoolTimeDependentVisitStartTime.put(master, master.getStartTime());
        this.carpoolTimeDependentVisitStartTime.put(dependent, Math.max(dependent.getStartTime(), master.getStartTime() + intervalStart));
    }

    // Method to add new carpoolTimeDependentVisitPair, because this list is initially empty. Input: TimeDependentVisitPair
    protected void addCarpoolTimeDependentVisitPair(TimeDependentVisitPair pair){
        this.carpoolTimeDependentVisitPairs.add(pair);
        this.carpoolTimeDependentVisitStartTime.put(pair.getMasterVisit(), pair.getMasterVisit().getStartTime());
        this.carpoolTimeDependentVisitStartTime.put(pair.getDependentVisit(), Math.max(pair.getDependentVisit().getStartTime(), pair.getMasterVisit().getStartTime() + pair.getIntervalStart()));
    }

    // Method to remove carpoolTimeDependentVisitPair, and ensuring it gets deleted from both lists
    protected void removeCarpoolTimeDependentVisitPair(Visit visit) {
        for (TimeDependentVisitPair pair: this.carpoolTimeDependentVisitPairs){
            if (pair.getMasterVisit().equals(visit)){
                this.carpoolTimeDependentVisitStartTime.remove(pair.getDependentVisit());
                this.carpoolTimeDependentVisitStartTime.remove(visit);
                this.carpoolTimeDependentVisitPairs.remove(pair);
            }
            else if (pair.getDependentVisit().equals(visit)){
                this.carpoolTimeDependentVisitStartTime.remove(pair.getMasterVisit());
                this.carpoolTimeDependentVisitStartTime.remove(visit);
                this.carpoolTimeDependentVisitPairs.remove(pair);
            }
        }
    }

    protected void setSyncedVisitStartTime(Visit visit, int startTime) {
        this.timeDependentVisitStartTime.put(visit, startTime);
    }

    // Carpool-sync-starttime-setter
    protected void setCarpoolSyncedVisitStartTime(Visit visit, int startTime) {
        this.carpoolTimeDependentVisitStartTime.put(visit, startTime);
    }

    public int getSyncedTaskStartTime(Visit visit) {
        return this.timeDependentVisitStartTime.get(visit);
    }

    // Carpool-sync-starttime-getter
    public int getCarpoolSyncedTaskStartTime(Visit visit) {
        return this.carpoolTimeDependentVisitStartTime.get(visit);
    }

    protected Visit removeFromRoute(Shift shift, int index) {
        return getRoute(shift).remove(index);
    }

    protected Visit removeFromRoute(int shiftID, int index) {
        return getRoute(shiftID).remove(index);
    }

    public List<Visit> getRoute(Shift shift) {
        return getRoute(shift.getId());
    }

    public List<Visit> getRoute(int shiftid) {
        return shiftRoutes.get(shiftid);
    }

    public Map<Visit, Integer> getSyncedVisitStartTimes() {
        return timeDependentVisitStartTime;
    }

    @Override
    public String toString() {
        return "This is a solution";
    }

    public Set<Task> getAllocatedTasks() {
        return allocatedTasks;
    }
    public Set<Task> getUnallocatedTasks() {
        return unallocatedTasks;
    }

    public Set<Visit> getUnallocatedVisits() {
        return unallocatedVisits;
    }

    public void addVisitsToUnallocatedVisits(Collection<Visit> visits) {
        unallocatedVisits.addAll(visits);
    }

    public static void main(String[] args) {
        Model model = new Model(4);
        model.loadData();
        Solution solution = new Solution(model);
        System.out.println(solution.toString());
    }

}
