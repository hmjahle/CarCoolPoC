package com.visma.of.cps.algorithm.repair;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.TimeDependentVisitPair;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.routeEvaluator.results.MultiRouteEvaluatorResult;
import com.visma.of.cps.routeEvaluator.results.Route;
import com.visma.of.cps.routeEvaluator.results.RouteEvaluatorResult;
import com.visma.of.cps.routeEvaluator.solver.RouteEvaluator;
import com.visma.of.cps.solution.Problem;
import com.visma.of.cps.solution.Solution;
import com.visma.of.cps.util.CarPoolingTimeDependentPairsUtils;
import com.visma.of.cps.util.Constants;
import com.visma.of.cps.util.CarPoolingTimeDependentPairsUtils.ShiftRouteEvaluatorPair;
import com.visma.of.cps.util.Constants.VisitType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.visma.of.cps.util.RandomUtils.objectiveNoise;



public class GreedyRepairAlgorithm implements IRepairAlgorithm {

    protected final Model model;
    private final Random random;
    //private final FeasibilityChecker feasibilityChecker;
    private final CarPoolingTimeDependentPairsUtils carpoolingUtils = new CarPoolingTimeDependentPairsUtils();

    public GreedyRepairAlgorithm(Model model, Random random) {
        this.model = model;
        this.random = random;
        //this.feasibilityChecker = FeasibilityChecker.getInstance();
    }

    /**
     * WARNING: Modifies unallocatedTasks by removing allocated task from it.
     */
    public NeighborhoodMoveInfo repair(NeighborhoodMoveInfo neighborhoodMoveInfo, List<Shift> shifts, Set<Visit> unallocatedVisits) {
        if (unallocatedVisits.isEmpty()) {
            return null;
        }

        Double repairDelta = findBestGreedyInsert(neighborhoodMoveInfo.getProblem(), shifts, unallocatedVisits);
        if (repairDelta == null) {
            return null;
        }

        neighborhoodMoveInfo.setDeltaObjectiveValue(neighborhoodMoveInfo.getDeltaObjectiveValue() + repairDelta);
        return neighborhoodMoveInfo;
    }

    protected Double findBestGreedyInsert(Problem problem, List<Shift> shifts, Set<Visit> unallocatedVisits) {
        var solution = problem.getSolution();
        var objective = problem.getObjective();
        double bestDeltaObjectiveValue = Double.MAX_VALUE;
        MultiRouteEvaluatorResult bestInsertVisitResult = null;

        double deltaObjectiveValue;

        List<Visit> visits = new ArrayList<>(unallocatedVisits);
        Collections.shuffle(visits);
        List<Visit> legalMotorizedVisits = visits.stream().filter(v -> v.completesTask()).collect(Collectors.toList());
        for (Shift shift : shifts) {
            if (shift.isMotorized()){
                for (Visit insertVisit : legalMotorizedVisits){
                    MultiRouteEvaluatorResult result = findRouteForMotorized(problem, insertVisit, solution, shift);
                    if (result == null || result.isInfeasibleInsert()) continue;
                    deltaObjectiveValue = result.getDeltaObjective(objective);

                    if (objectiveNoise(random) * deltaObjectiveValue < bestDeltaObjectiveValue) {
                        bestDeltaObjectiveValue = deltaObjectiveValue;
                        bestInsertVisitResult = result;
                    }
                }
            } else {
                for (Visit insertVisit : unallocatedVisits){
                    if (!legalInsertNonMotorizedShift(solution, insertVisit, shift)) continue;
                    MultiRouteEvaluatorResult result = findRouteForNonMotorized(problem, insertVisit, solution, shift);
                    if (result == null || result.isInfeasibleInsert()) continue;
                    deltaObjectiveValue = result.getDeltaObjective(objective);

                    if (objectiveNoise(random) * deltaObjectiveValue < bestDeltaObjectiveValue) {
                        bestDeltaObjectiveValue = deltaObjectiveValue;
                        bestInsertVisitResult = result;
                    }
                }
            }
        }
        if (bestInsertVisitResult == null) return null;

        Double deltaObjective = updateSolution(problem, bestDeltaObjectiveValue, bestInsertVisitResult);
        for ( Visit insertedVisit : bestInsertVisitResult.getAllInsertedVisits()){
            unallocatedVisits.remove(insertedVisit); // remove unallocated if not already removed
        }
        return deltaObjective;
    }



    private MultiRouteEvaluatorResult findRouteForMotorized(Problem problem, Visit insertVisit, Solution solution, Shift motorizedShift){
        List<Visit> insertedVisitsMotorized = new ArrayList<>();
        insertedVisitsMotorized.add(insertVisit);
        RouteEvaluatorResult resultMotorized = findRoute(problem, insertVisit, solution, motorizedShift);
        // If the insert was infeasible return null
        if (resultMotorized == null) return null; 
        int insertIndex = resultMotorized.getRoute().findIndexInRouteVisit(insertVisit);
        Visit predecessor = insertIndex == 0 ? null : resultMotorized.getRoute().getVisitSolution().get(insertIndex-1);
        // Inserted a complete task not during carpooling. Case 2: m in PP
        if(predecessor == null || !predecessor.isPickUp()){return new MultiRouteEvaluatorResult(resultMotorized, motorizedShift.getId(), insertedVisitsMotorized);}
        // Else we inserted a complete task during a carpooling.
        
        // Find corresponding drop-off, pick-up and JM
        Map<Integer, Visit> correspondingTransportVisits = findCorrespondingTransportVisits(insertVisit);

        Visit pickUp = correspondingTransportVisits.get(VisitType.PICK_UP);
        Visit dropOff = correspondingTransportVisits.get(VisitType.DROP_OF);
        Visit newJM = correspondingTransportVisits.get(VisitType.JOIN_MOTORIZED);
        Visit completeTask = correspondingTransportVisits.get(VisitType.COMPLETE_TASK);
        
        // Insert pick up after complete task. (NB has to be done before insertion of drop off, so that the index is still correct)
        resultMotorized.getRoute().addVisitAtIndex(pickUp, insertIndex+1);
        // Insert drop off behind complete task    
        resultMotorized.getRoute().addVisitAtIndex(dropOff, insertIndex-1);

        // NB!!! In the following code we change the time window of the visits. Should they be copied and not changed directly??

        // Set time windows for D, P and JM
        setTimeWindows(carpoolingUtils.calculateTimeWindowsForMotorized(resultMotorized.getRoute().getVisitSolution(), newJM, dropOff, pickUp, completeTask, solution.getCarpoolSyncedTaskStartTimes(), motorizedShift));
        
        // Create time dependent pairs
        Map<Visit, Integer> carpoolSyncedVisitStartTime = new HashMap<>();
        List<TimeDependentVisitPair> newTimeDependentVisitPairs = new ArrayList<>();

        int syncedStartTimeDropOff = carpoolingUtils.getTimeWindowStart(resultMotorized.getRoute().getVisitSolution(), dropOff, solution.getCarpoolSyncedTaskStartTimes(), motorizedShift);
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(dropOff, motorizedShift.getId(), completeTask, motorizedShift.getId(), syncedStartTimeDropOff, 0, carpoolSyncedVisitStartTime));
        int syncedStartTimePickUp = syncedStartTimeDropOff + insertVisit.getVisitDuration();
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(pickUp, motorizedShift.getId(), newJM, motorizedShift.getId(), syncedStartTimePickUp, 0, carpoolSyncedVisitStartTime));
        
        // Find non motorized carpooling shift ID
        Integer coCarPoolerShiftID = predecessor.getCoCarPoolerShiftID();
        // Add the corresponding JM to the non motorized route after the predecessor JM 
        Route newNonMotorizedRoute = new Route();
        newNonMotorizedRoute.addVisits(solution.getRoute(coCarPoolerShiftID));
        int indexPredecessorJM = newNonMotorizedRoute.findIndexInRouteVisitTypeTask(predecessor.getTask(), VisitType.JOIN_MOTORIZED);
        newNonMotorizedRoute.addVisitAtIndex(newJM, indexPredecessorJM+1);

        RouteEvaluatorResult resultNonMotorized = getEvaluatorResultByTheOrderOfVisits(newNonMotorizedRoute.getVisitSolution(), problem, motorizedShift);

        MultiRouteEvaluatorResult multiRouteEvaluatorResult = new MultiRouteEvaluatorResult(resultMotorized, resultNonMotorized, newTimeDependentVisitPairs, carpoolSyncedVisitStartTime, motorizedShift.getId(), coCarPoolerShiftID);
        multiRouteEvaluatorResult.setInsertedVisits(motorizedShift.getId(), insertedVisitsMotorized);
        multiRouteEvaluatorResult.setInsertedVisits(coCarPoolerShiftID, new ArrayList<>(Arrays.asList(newJM)));
        return multiRouteEvaluatorResult;
    }


    
    private MultiRouteEvaluatorResult findRouteForNonMotorized(Problem problem, Visit insertVisit, Solution solution, Shift nonMotorizedShift){
        if (insertVisit.completesTask()) return insertCompleteTaskNonMotorized(problem, insertVisit, solution, nonMotorizedShift);
        return insertJoinMotorizedNonMotorized(problem, insertVisit, solution, nonMotorizedShift);
    }

    private MultiRouteEvaluatorResult insertCompleteTaskNonMotorized(Problem problem, Visit insertVisit, Solution solution, Shift nonMotorizedShift){
        List<Visit> insertedVisitsNonMotorized = new ArrayList<>();
        insertedVisitsNonMotorized.add(insertVisit);
        RouteEvaluatorResult resultNonMotorized = findRoute(problem, insertVisit, solution, nonMotorizedShift);
        // If the insert was infeasible return null
        if (resultNonMotorized == null) return null;
        int insertIndex = resultNonMotorized.getRoute().findIndexInRouteVisit(insertVisit);
        Visit predecessor = insertIndex == 0 ? null : resultNonMotorized.getRoute().getVisitSolution().get(insertIndex-1);
        Visit successor = insertIndex < resultNonMotorized.getRoute().getVisitSolution().size() ? null : resultNonMotorized.getRoute().getVisitSolution().get(insertIndex+1);
        if (predecessor == null || !predecessor.isJoinMotorized()){return new MultiRouteEvaluatorResult(resultNonMotorized, nonMotorizedShift.getId(), insertedVisitsNonMotorized);}

        // Else we have "broken-up" a carpooling route. Case 4 in PP
        if (successor == null) throw new IllegalStateException("Non motorized is never dropped of");
        // Find corresponding drop-off, pick-up and JM
        Map<Integer, Visit> correspondingTransportVisits = findCorrespondingTransportVisits(insertVisit);

        Visit pickUp = correspondingTransportVisits.get(VisitType.PICK_UP);
        Visit dropOff = correspondingTransportVisits.get(VisitType.DROP_OF);
        Visit newJM = correspondingTransportVisits.get(VisitType.JOIN_MOTORIZED);

        // Insert new JM after the complete task
        resultNonMotorized.getRoute().addVisitAtIndex(newJM, insertIndex+1);
        insertedVisitsNonMotorized.add(newJM);

        // Create time dependent pairs
        // Find driver shift id
        int motorizedShiftId = predecessor.getCoCarPoolerShiftID();
        Map<Visit, Integer> carpoolSyncedVisitStartTime = new HashMap<>();
        List<TimeDependentVisitPair> newTimeDependentVisitPairs = new ArrayList<>();

        // Find previous pick up visit
        Visit prevPickUp = findCorrespondingTransportVisits(predecessor).get(VisitType.PICK_UP);
        // Calculate time window for previous JM + Complete task (insertVisit) + previous P + insert Drop off
        setTimeWindows(carpoolingUtils.calculateTimeWindowsForNonMotorized(resultNonMotorized.getRoute().getVisitSolution(), predecessor, dropOff, prevPickUp, insertVisit, solution.getCarpoolSyncedTaskStartTimes(), nonMotorizedShift));
        
        // Sync insert visit and insert drop off
        int syncedStartTimeDropOff = insertVisit.getTimeWindowStart();
        int intervalOffset = insertVisit.getTimeWindowEnd() - syncedStartTimeDropOff;
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(dropOff, motorizedShiftId, insertVisit, nonMotorizedShift.getId(), syncedStartTimeDropOff, intervalOffset, carpoolSyncedVisitStartTime));
        
        // Find successor motorized
        Visit nextDropOff = findCorrespondingTransportVisits(successor).get(VisitType.DROP_OF);
        // Calculate time window for new JM + P + successor Drop off + successor nonMotorized
        setTimeWindows(carpoolingUtils.calculateTimeWindowsForNonMotorized(resultNonMotorized.getRoute().getVisitSolution(), newJM, pickUp, nextDropOff, successor, solution.getCarpoolSyncedTaskStartTimes(), nonMotorizedShift));

        // Sync new JM and insert pick up
        int syncedStartTimeJM = newJM.getTimeWindowStart();
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(pickUp, motorizedShiftId, newJM, nonMotorizedShift.getId(), syncedStartTimeJM, 0, carpoolSyncedVisitStartTime));

        // Add the new Pick upp and Drop off to the motorized shift
        Route newMotorizedRoute = new Route();
        newMotorizedRoute.addVisits(solution.getRoute(motorizedShiftId));
        int indexPreviousPickUp = newMotorizedRoute.findIndexInRouteVisitTypeTask(predecessor.getTask(), VisitType.PICK_UP);
        newMotorizedRoute.addVisitAtIndex(dropOff, indexPreviousPickUp+1);
        newMotorizedRoute.addVisitAtIndex(pickUp, indexPreviousPickUp+1);

        RouteEvaluatorResult resultMotorized = getEvaluatorResultByTheOrderOfVisits(newMotorizedRoute.getVisitSolution(), problem, model.getShift(motorizedShiftId));
        if (resultMotorized==null) return null;

        MultiRouteEvaluatorResult multiRouteEvaluatorResult = new MultiRouteEvaluatorResult(resultNonMotorized, resultMotorized, newTimeDependentVisitPairs, carpoolSyncedVisitStartTime, nonMotorizedShift.getId(), motorizedShiftId);
        multiRouteEvaluatorResult.setInsertedVisits(nonMotorizedShift.getId(), insertedVisitsNonMotorized);
        multiRouteEvaluatorResult.setInsertedVisits(motorizedShiftId, new ArrayList<>(Arrays.asList(dropOff, pickUp)));
        return multiRouteEvaluatorResult;
    }
    
    
    private MultiRouteEvaluatorResult insertJoinMotorizedNonMotorized(Problem problem, Visit insertVisit, Solution solution, Shift nonMotorizedShift){
        List<Visit> route = solution.getRoute(nonMotorizedShift.getId());

        // Find corresponding complete task visit, pick-up and drop off.
        Map<Integer, Visit> correspondingTransportVisits = findCorrespondingTransportVisits(insertVisit);

        Visit completeTask = correspondingTransportVisits.get(VisitType.COMPLETE_TASK);
        Visit pickUp = correspondingTransportVisits.get(VisitType.PICK_UP);
        int completeTaskIndex = route.indexOf(completeTask);
        
        Visit successorNonMotorized = route.get(completeTaskIndex+1);
        Visit dropOff = findCorrespondingTransportVisits(successorNonMotorized).get(VisitType.DROP_OF);
        
        // Insert JM after the CT
        Route newNonMotorizedRoute = new Route();
        newNonMotorizedRoute.addVisits(route);
        newNonMotorizedRoute.addVisitAtIndex(insertVisit, completeTaskIndex+1);

        RouteEvaluatorResult resultNonMotorized = getEvaluatorResultByTheOrderOfVisits(newNonMotorizedRoute.getVisitSolution(), problem, nonMotorizedShift);
        if (resultNonMotorized == null) return null;

        setTimeWindows(carpoolingUtils.calculateTimeWindowsForNonMotorized(resultNonMotorized.getRoute().getVisitSolution(), insertVisit, pickUp, dropOff, successorNonMotorized, solution.getCarpoolSyncedTaskStartTimes(), nonMotorizedShift));

        ShiftRouteEvaluatorPair motorizedShiftResult = getBestMotorizedShift(problem, pickUp, dropOff);
        if (motorizedShiftResult == null) return null;
        Shift motorizedShift = motorizedShiftResult.getShift();
        RouteEvaluatorResult resultMotorized = motorizedShiftResult.getRouteEvaluatorResult();

        // Create time dependent pairs
        Map<Visit, Integer> carpoolSyncedVisitStartTime = new HashMap<>();
        List<TimeDependentVisitPair> newTimeDependentVisitPairs = new ArrayList<>();

        // Get synced start times
        int syncedStartTimePickUp = carpoolingUtils.getTimeWindowStart(resultMotorized.getRoute().getVisitSolution(), pickUp, solution.getCarpoolSyncedTaskStartTimes(), motorizedShift);
        int syncedStartTimeDropOff = carpoolingUtils.getTimeWindowStart(resultMotorized.getRoute().getVisitSolution(), dropOff, solution.getCarpoolSyncedTaskStartTimes(), motorizedShift);
        int intervalOffset = successorNonMotorized.getTimeWindowEnd() - syncedStartTimeDropOff;

        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(pickUp, motorizedShift.getId(), insertVisit, nonMotorizedShift.getId(), syncedStartTimePickUp, 0, carpoolSyncedVisitStartTime));
        newTimeDependentVisitPairs.add(carpoolingUtils.createCarpoolTimeDependentPair(dropOff, motorizedShift.getId(), successorNonMotorized, nonMotorizedShift.getId(), syncedStartTimeDropOff, intervalOffset, carpoolSyncedVisitStartTime));


        MultiRouteEvaluatorResult multiRouteEvaluatorResult = new MultiRouteEvaluatorResult(resultNonMotorized, resultMotorized, newTimeDependentVisitPairs, carpoolSyncedVisitStartTime, nonMotorizedShift.getId(), motorizedShift.getId());
        multiRouteEvaluatorResult.setInsertedVisits(nonMotorizedShift.getId(), new ArrayList<>(Arrays.asList(insertVisit)));
        multiRouteEvaluatorResult.setInsertedVisits(motorizedShift.getId(), new ArrayList<>(Arrays.asList(dropOff, pickUp)));
        return multiRouteEvaluatorResult;
    }




    protected Double updateSolutionOneShift(Problem problem, double bestObjective, int bestShiftId, List<Visit> bestVisits, RouteEvaluatorResult bestRoute) {
        Shift bestShift = model.getShift(bestShiftId);
        for (Visit bestVisit: bestVisits){
            // NB NB!!!! Must be inserted in the correct order, because the index is dependent on the size of the list.
            Integer index = bestRoute.getRoute().findIndexInRouteVisit(bestVisit);
            problem.assignVisitToShiftByIndex(bestShift, bestVisit, index, bestObjective);
        }
        return bestObjective;
    }

    protected Double updateSolution(Problem problem, double bestObjective, MultiRouteEvaluatorResult bestInsertVisitResult ) {
        int shiftIdOne = bestInsertVisitResult.getShiftIdOne();
        updateSolutionOneShift(problem, bestObjective, shiftIdOne, bestInsertVisitResult.getInsertedVisits(shiftIdOne), bestInsertVisitResult.getRouteEvaluatorOne());
        if (bestInsertVisitResult.isMultipleRoutesAffected()) {
            int shiftIdTwo = bestInsertVisitResult.getShiftIdTwo();
            updateSolutionOneShift(problem, bestObjective, shiftIdTwo, bestInsertVisitResult.getInsertedVisits(shiftIdTwo), bestInsertVisitResult.getRouteEvaluatorTwo());
        }
        return bestObjective;
    }


    
    /**
     * Insert a list of vistis into a shift and calculate the objective value result. The visit order
     * from the list will be upheld in the resulting route
     * @param visits Visits to be inserted into the shift
     * @param problem The current problem
     * @param shift The shift to insert the visits into
     * @return A route evaluator result. Possibly null if insert is infeasible
     */
    protected RouteEvaluatorResult getEvaluatorResultWithMultipleVisits(List<Visit> visits, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisitsInsertVisits(
                solution.getRoute(shift), visits, solution.getCarpoolSyncedTaskStartTimes(), shift);
    }

    protected RouteEvaluatorResult getEvaluatorResultByTheOrderOfVisits(List<Visit> visits, Problem problem, Shift shift){
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisits(visits, solution.getCarpoolSyncedTaskStartTimes(), shift);
    }


    /**
     * This method:
     *  1.  Insert the visit into the shift and runs the route evaluator
     *  2.  Discover which case we have, based on the visits location in the route
     *  3.  Handles potential inserts in other shifts (e.g., Pick-up and drop-off inserts in motorized shifts) 
     * @param problem The current problem
     * @param visit The visit we wish to insert
     * @param solution The current solution
     * @param shift The non-motorized shift we wish to insert the visit into
     * @return A list of route evaluator results with affected routes. Empty list if none is feasible
     */
    private List<RouteEvaluatorResult> findRouteForNonMotorized2(Problem problem, Visit visit, Solution solution, Shift shift) {
        List<RouteEvaluatorResult> affectedRoutes = new ArrayList<>();

        RouteEvaluatorResult result = getEvaluatorResult(visit, problem, shift);
        //int case = getInsertCaseForNonMotorizedShift(result.getRoute() visit);
        return affectedRoutes;
    }

    /**
     * This method tries to insert the pick-up and drop-of pair into all motorized shifts, run the route evaluator, and
     * return the best shift
     * @param problem The current problem
     * @param motorizedShifts The possible motorized shifts that can take this transport request
     * @param pickUp 
     * @param dropOf
     * @return Return a route evaluator result with the best shift. Null if no shift is found
     */
    private ShiftRouteEvaluatorPair getBestMotorizedShift(Problem problem, Visit pickUp, Visit dropOf) {
        RouteEvaluatorResult bestResult = null;
        List<Visit> transportRequest = new ArrayList<>(Arrays.asList(pickUp, dropOf)); // Change this to be aggregated node???
        Shift bestShift = null;

        for (Shift mShift : model.getCarpoolAbleMotorizedShifts()) {
            RouteEvaluatorResult result = getEvaluatorResultWithMultipleVisits(transportRequest, problem, mShift);

            if (result == null) continue;
            if (bestResult == null) {
                bestResult = result;
                bestShift = mShift;
                continue;
            }
            if (bestResult.getObjectiveValue() < result.getObjectiveValue()) {
                bestResult = result;
                bestShift = mShift;
            } 
        } 
        return bestResult == null ? null : carpoolingUtils.new ShiftRouteEvaluatorPair(bestShift, bestResult);
    }

    protected RouteEvaluatorResult getEvaluatorResult(Visit visit, Problem problem, Shift shift) {
        var solution = problem.getSolution();
        return problem.getRouteEvaluators().get(shift.getId()).evaluateRouteByTheOrderOfVisitsInsertVisit(
                solution.getRoute(shift), visit, solution.getCarpoolSyncedTaskStartTimes(), shift);
    }

    private RouteEvaluatorResult findRoute(Problem problem, Visit visit, Solution solution, Shift shift) {
        return getEvaluatorResult(visit, problem, shift);
    }

    private boolean legalInsertNonMotorizedShift(Solution solution, Visit visit, Shift shift){
        return visit.completesTask() || (visit.isJoinMotorized() && completeTaskIsInShift(solution, shift, visit));
    }

    private boolean completeTaskIsInShift(Solution solution, Shift shift, Visit joinMotorized){
        for (Visit visit: solution.getRoute(shift.getId())){
            if (visit.getTask() == joinMotorized.getTask()){return true;}
        }
        return false;
    }

    private void setTimeWindows(Map<Visit, List<Integer>> timeWindows){
        for(Map.Entry<Visit, List<Integer>> entry : timeWindows.entrySet()){
            entry.getKey().setTimeWindowStart(entry.getValue().get(0));
            entry.getKey().setTimeWindowEnd(entry.getValue().get(1));
        }
    }

    private Map<Integer, Visit> findCorrespondingTransportVisits(Visit visit){
        // Find corresponding drop-off, pick-up and JM
        Map<Integer, Visit> correspondingTransportVisits = model.getVisits().stream().filter(v -> v.getTask() == visit.getTask()).collect(Collectors.toMap(Visit::getVisitType, Function.identity()));
        
        // Check if state is correct. Can be removed, because this should never happen. But is nice for debugging
        if (correspondingTransportVisits.size() != 4 || 
            correspondingTransportVisits.containsKey(VisitType.COMPLETE_TASK) ||
            correspondingTransportVisits.containsKey(VisitType.DROP_OF) ||
            correspondingTransportVisits.containsKey(VisitType.PICK_UP) ||
            correspondingTransportVisits.containsKey(VisitType.JOIN_MOTORIZED)) {throw new IllegalStateException("The model do not have 4 visits for each task");}

        return correspondingTransportVisits;
    }


}