package com.visma.of.cps.algorithm.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.visma.of.cps.algorithm.NeighborhoodMoveInfo;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.solution.Objective;
import com.visma.of.cps.solution.Problem;
import com.visma.of.cps.solution.Solution;
import com.visma.of.cps.util.Constants;

public abstract class GreedyDestroyAbstract extends DestroyOperatorAbstract {

    protected Model model;
    protected int[] shiftIndices;

    protected GreedyDestroyAbstract(Model model, Random random) {
        super(random);
        this.model = model;
        if (model == null || model.getShifts().isEmpty()) {
            shiftIndices = new int[0];
        } else {
            shiftIndices = new int[model.getShifts().size()];
            resetRandomShiftFinder();
        }
    }

    private int resetRandomShiftFinder() {
        for (int i = 0; i < shiftIndices.length; i++) {
            shiftIndices[i] = i;
        }
        return shiftIndices.length;
    }

    protected GreedyDestroyAbstract(Model model) {
        this(model, new Random());
    }

    /**
     * Used to find an additional destroy move. The neighborhood move must contain a @NotNull solution and objective. Note that if a new move is found, the
     * provided neighborhood move and the solution and objective contained therein is updated (changed)
     *
     * @param neighborhoodMoveInfo Info on the current move on which to perform further changed. Note that the contained solution can be changed.
     * @return The updated neighborhood move, same as provided. Or null if no change is possible.
     */
    protected NeighborhoodMoveInfo internalDestroy(NeighborhoodMoveInfo neighborhoodMoveInfo) {
        return internalDestroy(neighborhoodMoveInfo, neighborhoodMoveInfo.getProblem());
    }

    /**
     * Used to find a destroy move from a solution. If a move is possible the solution will be altered and a neighborhood move
     * containing the copied solution is returned.
     *
     * @param problem Containing original solution to destroy. The input solution in the problem can be changed.
     * @return New neighborhood move or null if no move is possible.
     */
    protected NeighborhoodMoveInfo internalDestroy(Problem problem) {
        return internalDestroy(null, problem);
    }

    /**
     * Finds a random shift and then removes the task from that shift that reduce the objective value the most.
     * If no improvement is possible it continues to the next shift until there are no more shifts to investigate.
     *
     * @param neighborhoodMoveInfo Can be null, if null and a move is found. Then a new neighborhood move is generated from the solution provided.
     *                             Otherwise it is assumed that this is an additional move to the neighborhood move provided.
     * @param problem              Containing a solution to be used to find the move. If a neighborhoodMoveInfo (that is not null) is provided, this must be the same solution as
     *                             contained inside the neighborhoodMoveInfo.
     * @return A new NeighborhoodMoveInfo if a move is possible, otherwise null.
     */
    private NeighborhoodMoveInfo internalDestroy(NeighborhoodMoveInfo neighborhoodMoveInfo, Problem problem) {
        Solution solution = problem.getSolution();
        Objective objective = problem.getObjective();
        double bestValue = Double.MAX_VALUE;
        Map<Integer, List<Integer>> removeVisits = new HashMap<>();
        int totalShifts = resetRandomShiftFinder();
        while (totalShifts != 0) {
            int randInt = random.nextInt(totalShifts);
            int shiftIndex = shiftIndices[randInt];
            Shift shift = model.getShifts().get(shiftIndex);
            totalShifts = updateRandomShifts(totalShifts, randInt, shiftIndex);
            List<Visit> route = solution.getRoute(shiftIndex);
            for (int i = 0; i < route.size(); i++) {
                Double deltaIntraObjective = 0.0; // Will contain change in total objective if visits are removed from all necessary shifts
                Double intraObj = null;
                Map<Integer, List<Integer>> removeVisitsTemp = findVisitsToRemove(solution, shift, i);
                for (Map.Entry<Integer, List<Integer>> entry: removeVisitsTemp.entrySet()){
                    Shift entryShift = model.getShifts().get(entry.getKey());
                    List<Integer> removalPositions = entry.getValue();
                    intraObj = calculateIntraObjective(solution, entryShift, removalPositions, objective);
                    if(intraObj == null) continue; // intraObj is null if solution is infeasible
                    deltaIntraObjective += intraObj;
                }
                if(intraObj == null) continue;// intraObj is null if solution is infeasible
                if (noise(random) * deltaIntraObjective < bestValue) {
                    bestValue = deltaIntraObjective;
                    removeVisits = removeVisitsTemp;
                }
            }
            if (bestValue < 0)
                break;
        }
        if (removeVisits.size()>0 && (neighborhoodMoveInfo == null || !neighborhoodMoveInfo.possible())) 
            // !neighborhoodMoveInfo.possible() == true if deltaObjectiveValue = null
            neighborhoodMoveInfo = new NeighborhoodMoveInfo(problem);
        return update(neighborhoodMoveInfo, bestValue, removeVisits);
    }

    /**
     * @param solution
     * @param entryShift
     * @param removalPositions list of indices for the visits to be removed from the route
     * @param objective
     * @return new objective value if visits are removed
     */
    private Double calculateIntraObjective(Solution solution, Shift entryShift, List<Integer> removalPositions, Objective objective){
        Double intraObj = null;
        if(!entryShift.isMotorized()){
            // Temporary update transportation mode, to calculate correct new objective
            intraObj = objective.deltaIntraObjectiveNewRoute(entryShift, temporarilyUpdateNonMotorizedRemovedVisits(solution, entryShift, removalPositions), removalPositions, solution.getCarpoolSyncedTaskStartTimes());
        } else {
            intraObj = objective.deltaIntraObjectiveNewRoute(entryShift, solution.getRoute(entryShift), removalPositions, solution.getCarpoolSyncedTaskStartTimes());
        }
        return intraObj;
    } 

    /**
     * @param solution
     * @param entryShift
     * @param removalPositions list of indices for the visits to be removed from the route
     * @return copy of the route with visit transport modes updated according to the removed visits
     */
    private List<Visit> temporarilyUpdateNonMotorizedRemovedVisits(Solution solution, Shift entryShift, List<Integer> removalPositions){
        List<Visit> routeCopy = new ArrayList<>();
        solution.getRoute(entryShift).stream().forEach(v -> routeCopy.add(new Visit(v)));
        for (int index : removalPositions){
            if(routeCopy.get(index).getVisitType() == Constants.VisitType.JOIN_MOTORIZED && routeCopy.size() > index){
                routeCopy.get(index+1).setTransportType(Constants.TransportMode.WALK);
                routeCopy.get(index+1).setCoCarPoolerShiftID(null);
            }}
        return routeCopy;
    }
    

    /**
     * Find which visits that must be removed, depending on if the shift is motorized and if the visit is a carpool task or not.
     * @param solution
     * @param shift
     * @param currentVisitIndex
     * @return Map of shift and which visits to remove from the corresponding shift. 
     */
    private Map<Integer, List<Integer>> findVisitsToRemove(Solution solution, Shift shift, Integer currentVisitIndex){
        return shift.isMotorized() ? findVisitsToRemoveMotorizedShift(solution, shift, currentVisitIndex) : findVisitsToRemoveNonMotorizedShift(solution, shift, currentVisitIndex);
    }

    private Map<Integer, List<Integer>> findVisitsToRemoveMotorizedShift(Solution solution, Shift shift, Integer currentVisitIndex){
        Map<Integer, List<Integer>> removeVisits = new HashMap<>();
        removeVisits.put(shift.getId(), new ArrayList<>(currentVisitIndex));
        List<Visit> route = solution.getRoute(shift);
        Integer predecessorIndex = 0 < currentVisitIndex ? currentVisitIndex-1 : null;
        Integer successorIndex = route.size() > currentVisitIndex ? currentVisitIndex+1 : null;
        if (route.get(currentVisitIndex).getVisitType() == Constants.VisitType.DROP_OF){
            // The current visit is a drop off visit in a motorized shift
            // Case 1 driver in PP
            // Remove Pick-up node
            // When current is a drop-off, the previous node always has to be a pick-up.
            if (predecessorIndex == null || route.get(predecessorIndex).getVisitType() != Constants.VisitType.PICK_UP) {throw new IllegalStateException("The previous node in route should be a pick up");}
            removeVisits.get(shift.getId()).add(predecessorIndex);

            // Get the person that is dropped off
            int passengerShiftID = route.get(currentVisitIndex).getCoCarPoolerShiftID();
            // Get the pickup point for coDrive arc
            List<Integer> transportVisitIndices = solution.getTransportVisitIndices(passengerShiftID, route.get(predecessorIndex));
            // Remove the pickup point for that arc
            if (!transportVisitIndices.isEmpty()){
                removeVisits.put(passengerShiftID, transportVisitIndices);
            }
        }
        else if (route.get(currentVisitIndex).getVisitType() == Constants.VisitType.PICK_UP){
            // The current visit is a pick up visit in a motorized shift
            // Case 2 driver in PP

            // Remove Drop-off node
            // When current is a pick-up, the successor has to be a drop-off
            if (successorIndex == null || route.get(successorIndex).getVisitType() != Constants.VisitType.DROP_OF) {throw new IllegalStateException("The successor node in the route should be a drop off");}
            removeVisits.get(shift.getId()).add(successorIndex); // When current is a pick-up, the next node always has to be a drop off.
            // Get the person that is dropped off
            int passengerShiftID = route.get(currentVisitIndex).getCoCarPoolerShiftID();
            // Get the pickup point for coDrive arc
            List<Integer> transportVisitIndices = solution.getTransportVisitIndices(passengerShiftID, route.get(currentVisitIndex));
            // Remove the pickup point for that arc
            if (!transportVisitIndices.isEmpty()){
                removeVisits.put(passengerShiftID, transportVisitIndices);
            }
        }
        else if (route.get(currentVisitIndex).isSynced()) {
            // Removing a complete task during a carpool route.
            // Case 3 driver in PP 
            if (predecessorIndex == null || successorIndex == null || 
                route.get(predecessorIndex).getVisitType() != Constants.VisitType.DROP_OF || 
                route.get(successorIndex).getVisitType() != Constants.VisitType.PICK_UP) {throw new IllegalStateException("Removing a CT during a carpool the predecessor is not a drop off or/and the successor is not a pick up");}
            // Remove drop-off
            removeVisits.get(shift.getId()).add(predecessorIndex);
            // Remove pick-up
            removeVisits.get(shift.getId()).add(successorIndex);
            // Remove join motorized for coDrive arc
            int passengerShiftID = route.get(currentVisitIndex).getCoCarPoolerShiftID();
            List<Integer> transportVisitIndices = solution.getTransportVisitIndices(passengerShiftID, route.get(currentVisitIndex));
            if (!transportVisitIndices.isEmpty()){
                removeVisits.put(passengerShiftID, transportVisitIndices);
            }
        }
        // Else the visit is a complete task visit, and only that one should be removed
        return removeVisits;
    }

    private Map<Integer, List<Integer>> findVisitsToRemoveNonMotorizedShift(Solution solution, Shift shift, Integer currentVisitIndex){
        Map<Integer, List<Integer>> removeVisits = new HashMap<>();
        removeVisits.put(shift.getId(), new ArrayList<>(currentVisitIndex));
        List<Visit> route = solution.getRoute(shift);
        Integer successorIndex = route.size()>currentVisitIndex ? currentVisitIndex+1 : null;
        Integer predecessorIndex = 0 < currentVisitIndex ? currentVisitIndex-1 : null;

        if (successorIndex != null && route.get(successorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
            // The employee is non-motorized and does carpooling to the next visit
            removeVisits.get(shift.getId()).add(successorIndex); // Case 1 in PP
            if (predecessorIndex != null && route.get(predecessorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
                // The employee also did carpooling to current visit
                int coDriverShiftID = route.get(predecessorIndex).getCoCarPoolerShiftID();
                if (coDriverShiftID == route.get(successorIndex).getCoCarPoolerShiftID()){
                    // Carpooling was with the same driver from previous -> current -> successor node. 
                    // Case 4 walker in PP
                    List<Integer> transportVisitIndices = solution.getTransportVisitIndices(coDriverShiftID, route.get(currentVisitIndex));
                    removeVisits.put(coDriverShiftID, transportVisitIndices);
                }
            }
        }
        else if (predecessorIndex != null && route.get(predecessorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
            // Non-motorized employee carpooled to current visit.
            // Case 2 walker in PP
            removeVisits.get(shift.getId()).add(predecessorIndex);
        }
        return removeVisits;
    }


    private int updateRandomShifts(int totalShifts, int randInt, int shiftIndex) {
        shiftIndices[randInt] = shiftIndices[totalShifts - 1];
        shiftIndices[totalShifts - 1] = shiftIndex;
        totalShifts--;
        return totalShifts;
    }

    /**
     * If a move is found the neighborhood move is updated (the solution and objective is also changed).
     *
     * @param neighborhoodMoveInfo Neighborhood info to update.
     * @param bestIntraObjective   Intra route objective value for the best move.
     * @param bestExtraObjective   Extra route objective value for the best move.
     * @param removeShift          Shift from which the task should be removed, null if it is not possible to remove an
     * @param removeIndex          Task at this index should be removed from the shift.
     * @return Null if no improvement is found, otherwise the same NeighborhoodMoveInfo as provided.
     */
    private NeighborhoodMoveInfo update(NeighborhoodMoveInfo neighborhoodMoveInfo, double bestIntraObjective,
                                        Map<Integer, List<Integer>> removeVisits) {

        for (Map.Entry<Integer, List<Integer>> entry: removeVisits.entrySet()){
            Shift removeShift = model.getShifts().get(entry.getKey());
            neighborhoodMoveInfo.getProblem().unAssignVisitsByRouteIndices(removeShift, entry.getValue(), bestIntraObjective); // updates the objective when the task (remove index) is removed from the shift
            
        
        }
        double deltaObjectiveValue = bestIntraObjective + neighborhoodMoveInfo.getDeltaObjectiveValue();
        neighborhoodMoveInfo.setDeltaObjectiveValue(deltaObjectiveValue);
        return neighborhoodMoveInfo;
        
    }

}