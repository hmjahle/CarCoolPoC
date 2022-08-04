package com.visma.of.cps.solution;

import com.visma.of.cps.model.Model;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Task;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.model.TimeDependentVisitPair;

import java.util.*;

public class Solution {

    protected List<List<Visit>> shiftRoutes;
    private Shift[] taskAssignedToShift;
    private Shift[] visitAssignedToShift;
    private Set<Task> unallocatedTasks;
    private Set<Task> allocatedTasks;
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
    }

    public Solution(Solution other) {
        this.shiftRoutes = new ArrayList<>(other.shiftRoutes.size());
        for (List<Visit> route : other.shiftRoutes)
            this.shiftRoutes.add(new ArrayList<>(route));
        this.taskAssignedToShift = Arrays.copyOf(other.taskAssignedToShift, other.taskAssignedToShift.length);
        this.unallocatedTasks = new HashSet<>(other.unallocatedTasks);
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

    /**
     * Add a carpool time dependent pair to the solution. The start times can be both feasible or unfeasible
     * with respect to the intervalStart and intervalEnd. This depends on whether infeasible solutions can be
     * created or not
     * @param master The master visit
     * @param masterStartTime The actual time when the master visit have to start in its given shift
     * @param dependent The visit that is dependent on the master
     * @param dependentStartTime The actual time when the dependent visit have to start in the given shift
     * @param intervalStart start of the slack-interval for the dependent start time
     * @param intervalEnd end of the slack-interval for the dependent start time
     */
    protected void addCarpoolTimeDependentVisitPair(Visit master, int masterStartTime, Visit dependent, int dependentStartTime, int intervalStart, int intervalEnd){
        TimeDependentVisitPair carpoolPair = new TimeDependentVisitPair(master, dependent, intervalStart, intervalEnd);
        this.carpoolTimeDependentVisitPairs.add(carpoolPair);
        this.carpoolTimeDependentVisitStartTime.put(master, masterStartTime);
        this.carpoolTimeDependentVisitStartTime.put(dependent, dependentStartTime);
    }

    // Method to add new carpoolTimeDependentVisitPair, because this list is initially empty. Input: TimeDependentVisitPair
    protected void addCarpoolTimeDependentVisitPair(TimeDependentVisitPair pair, int masterStartTime){
        this.carpoolTimeDependentVisitPairs.add(pair);
        this.carpoolTimeDependentVisitStartTime.put(pair.getMasterVisit(), masterStartTime); 
        this.carpoolTimeDependentVisitStartTime.put(pair.getDependentVisit(), masterStartTime); 
    }

    /**
     * Method to remove carpoolTimeDependentVisitPair, and ensuring it gets deleted from both lists
     * @param visit Visit we want to remove, can be either a master og a dependent visit
     */
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

    // Carpool-sync-starttime-setter
    protected void setCarpoolSyncedVisitStartTime(Visit visit, int startTime) {
        this.carpoolTimeDependentVisitStartTime.put(visit, startTime);
    }

    // Carpool-Visit-pairs setter
    protected void setCarpoolTimeDependentVisitPairs(Collection<TimeDependentVisitPair> carpoolDependentVisitPairs){
        this.carpoolTimeDependentVisitPairs = carpoolDependentVisitPairs;
    }

    // Carpool-sync-starttime-getter
    public int getCarpoolSyncedTaskStartTime(Visit visit) {
        return this.carpoolTimeDependentVisitStartTime.get(visit);
    }

    // Carpool-sync-starttime-getter
    public Map<Visit, Integer> getCarpoolSyncedTaskStartTimes() {
        return this.carpoolTimeDependentVisitStartTime;
    }

    // Carpool-Visit-pairs getter
    public Collection<TimeDependentVisitPair> getCarpoolTimeDependentVisitPairs(){
        return this.carpoolTimeDependentVisitPairs;
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

    public List<Visit> getRoute(int shiftId) {
        return shiftRoutes.get(shiftId);
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
