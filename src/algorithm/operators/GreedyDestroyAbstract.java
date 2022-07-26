package algorithm.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algorithm.NeighborhoodMoveInfo;
import model.Model;
import model.Shift;
import model.Task;
import model.Visit;
import solution.Objective;
import solution.Problem;
import solution.Solution;
import util.Constants;

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
        double bestIntraObjective = 0.0;
        Map<Integer, List<Integer>> removeVisits = new HashMap<>();
        int totalShifts = resetRandomShiftFinder(); // intially length of shif indices
        while (totalShifts != 0) {
            int randInt = random.nextInt(totalShifts);
            int shiftIndex = shiftIndices[randInt];
            Shift shift = model.getShifts().get(shiftIndex);
            totalShifts = updateRandomShifts(totalShifts, randInt, shiftIndex); // Move rand shift to back of (toatal number of shifts-1) shift index list, and move last shift to this index. Returns total shifts--
            List<Visit> route = solution.getRoute(shiftIndex);
            for (int i = 0; i < route.size(); i++) {
                // Handle all cases, do we need to remove more than one Visit???
                Double deltaIntraObjective = objective.deltaIntraObjectiveRemovingVisitAtIndex(shift, route, i, solution.getSyncedVisitStartTimes());
                Double intraObj = null;
                Map<Integer, List<Integer>> removeVisitsTemp = findVisitsToRemove(solution, shift, i);
                for (Map.Entry<Integer, List<Integer>> entry: removeVisitsTemp.entrySet()){
                    Shift entryShift = model.getShifts().get(entry.getKey());
                    List<Integer> removalPositions = entry.getValue();

                    intraObj = objective.deltaIntraObjectiveNewRoute(entryShift, solution.getRoute(entryShift), removalPositions);
                    if(intraObj == null) continue;
                    deltaIntraObjective += intraObj;
                }
                if(intraObj == null) continue;
                // Keep things consitent. Remove from walker and driver if necessary.
                // OBS!!! This needs to be changed, because if we remove a walking task, we might need to remove p1, d1 and p2,d2
                if (skipTask(problem, shift, route, i, deltaIntraObjective)) continue; //The continue statement breaks one iteration
                // We only have intraObjectives for now
                //double deltaExtraObjective = objective.deltaExtraRouteObjectiveValueRemove(shift, route.get(i));
                //double deltaObjective = deltaExtraObjective + deltaIntraObjective;
                double deltaObjective = deltaIntraObjective;
                if (noise(random) * deltaObjective < bestValue) {
                    bestValue = deltaObjective;
                    bestIntraObjective = deltaIntraObjective;
                    //bestExtraObjective = deltaExtraObjective;
                    removeVisits = removeVisitsTemp;
                    // Run check for if any other tasks p1,d p2,d2 must be removed as well
                }
            }
            if (bestValue < 0)
                break;
        }
        if (removeVisits.size()>0 && (neighborhoodMoveInfo == null || !neighborhoodMoveInfo.possible())) 
        // !neighborhoodMoveInfo.possible() == true if deltaObjectiveValue = null, where delta objective value 
        // represents the change in objective that the changes in neighborhood of the lns has made.
            neighborhoodMoveInfo = new NeighborhoodMoveInfo(problem);
        // bestExtraObjective is always 0.0. Could remove variablle, but want to keep it as similar as possible.
        return update(neighborhoodMoveInfo, bestIntraObjective, removeVisits);
    }

    

    private Map<Integer, List<Integer>> findVisitsToRemove(Solution solution, Shift shift, Integer currentVisitIndex){
        Map<Integer, List<Integer>> removeVisits = new HashMap<>();
        removeVisits.put(shift.getId(), new ArrayList<>(currentVisitIndex));
        List<Visit> route = solution.getRoute(shift);
        Integer successorIndex = route.size()>currentVisitIndex ? currentVisitIndex+1 : null;
        Integer predecessorIndex = 0 < currentVisitIndex ? currentVisitIndex-1 : null;
        
        if (route.get(successorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
            // case 1
            removeVisits.get(shift.getId()).add(successorIndex);
            if (route.get(predecessorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
                int coDriverShiftID = route.get(predecessorIndex).getCoCarPoolerShiftID();
                if (coDriverShiftID == route.get(successorIndex).getCoCarPoolerShiftID()){
                    // Case 4
                    List<Integer> transportVisitIndices = solution.getTransportVisitIndices(coDriverShiftID, route.get(currentVisitIndex));
                    removeVisits.put(coDriverShiftID, transportVisitIndices);
                }
            }
        }
        else if (route.get(predecessorIndex).getVisitType() == Constants.VisitType.JOIN_MOTORIZED){
            // case 2
            removeVisits.get(shift.getId()).add(predecessorIndex);
        }
        else if (route.get(currentVisitIndex).getVisitType() == Constants.VisitType.DROP_OF ){
            // Case 1 driver
            // Get the person that is dropped off
            int passengerShiftID = route.get(currentVisitIndex).getCoCarPoolerShiftID();
            // Get the pickup point for codrive arc
            List<Integer> transportVisitIndices = solution.getTransportVisitIndices(passengerShiftID, route.get(predecessorIndex));
            // Remove the pickuppoint for that arc
            removeVisits.put(passengerShiftID, transportVisitIndices);
        }
        else if (route.get(currentVisitIndex).getVisitType() == Constants.VisitType.PICK_UP ){
            // Case 2 driver
            // Get the person that is dropped off
            int passengerShiftID = route.get(currentVisitIndex).getCoCarPoolerShiftID();
            // Get the pickup point for codrive arc
            List<Integer> transportVisitIndices = solution.getTransportVisitIndices(passengerShiftID, route.get(currentVisitIndex));
            // Remove the pickuppoint for that arc
            removeVisits.put(passengerShiftID, transportVisitIndices);
        }


        return removeVisits;

    }


    private boolean skipTask(Problem problem, Shift shift, List<Visit> route, int i, Double deltaIntraObjective) {
        // DeltaIntraObjective is Null is the solution is infeasable.
        // Calls this to check for the extrarouteconstraints, but we only have intra route
        //return !problem.getConstraints().isFeasibleRemoveTask(shift, route.get(i));
        return deltaIntraObjective == null ? true : false;
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