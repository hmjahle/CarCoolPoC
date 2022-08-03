package com.visma.of.cps.solution;

import com.visma.of.cps.model.Model;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Task;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.IConstraintIntraRoute;
import com.visma.of.cps.routeEvaluator.evaluation.objective.IObjectiveFunctionIntraRoute;
import com.visma.of.cps.routeEvaluator.solver.RouteEvaluator;
import com.visma.of.cps.util.Constants;
import com.visma.of.cps.util.Constants.TransportMode;

import java.util.*;
import java.util.stream.Collectors;

public class Problem {

    protected Solution solution;
    protected Objective objective;
    private final Map<String, IObjectiveFunctionIntraRoute> relaxedIntraRouteConstraints;

    public Problem(Model model) {
        this.solution = new Solution(model);
        this.objective = new Objective(model);
        relaxedIntraRouteConstraints = new LinkedHashMap<>();
    }

    public Problem(Problem problem) {
        this.solution = problem.solution != null ? new Solution(problem.solution) : null;
        this.objective = problem.objective != null ? new Objective(problem.objective) : null;

        this.relaxedIntraRouteConstraints = problem.relaxedIntraRouteConstraints;
    }

    public Map<String, IObjectiveFunctionIntraRoute> getRelaxedIntraRouteConstraints() {
        return relaxedIntraRouteConstraints;
    }

    public Solution getSolution() {
        return solution;
    }

    public Objective getObjective() {
        return objective;
    }

    public void addRelaxedIntraConstraint(String name, IConstraintIntraRoute constraint, IObjectiveFunctionIntraRoute objectiveFunction) {
        relaxedIntraRouteConstraints.put(name, objectiveFunction);
        for (RouteEvaluator routeEvaluator : objective.getRouteEvaluators().values()) {
            routeEvaluator.addConstraint(constraint);
        }
    }

    public Map<Integer, RouteEvaluator> getRouteEvaluators() {
        return objective.getRouteEvaluators();
    }


    public void addObjectiveIntraShiftRouteEvaluators(IObjectiveFunctionIntraRoute objectiveFunctionIntraRoute) {
        for (RouteEvaluator routeEvaluator : objective.getRouteEvaluators().values()) {
            routeEvaluator.addObjectiveIntraShift(objectiveFunctionIntraRoute);
        }
    }

    public void addObjectiveIntraShiftRouteEvaluators(String objectiveFunctionId, double weight, IObjectiveFunctionIntraRoute objectiveFunctionIntraRoute) {
        for (RouteEvaluator routeEvaluator : objective.getRouteEvaluators().values()) {
            routeEvaluator.addObjectiveIntraShift(objectiveFunctionId, weight, objectiveFunctionIntraRoute);
        }
    }

    public void update(Problem problem) {
        this.solution.update(problem.solution);
        this.objective.update(problem.objective);
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
        if (removedVisit.completesTask()){
            // The visit includes completing the task
            solution.unAllocateTask(removedVisit.getTask());
        }
        if (removedVisit.getCoCarPoolerShiftID() != null) {
            removedVisit.removeCarPooling();
            solution.removeCarpoolTimeDependentVisitPair(removedVisit);
            if (removedVisit.getVisitType()  == Constants.VisitType.JOIN_MOTORIZED && !shift.isMotorized() && route.size() > index){
                Visit successor = route.get(index+1);
                successor.setTransportType(Constants.TransportMode.WALK);
                if (successor.completesTask()){
                    successor.removeCarPooling();
                    solution.removeCarpoolTimeDependentVisitPair(successor);
                }

            }
        }
        objective.removeVisit(shift, removedVisit);
        objective.updateIntraRouteObjective(shift, intraObjectiveDeltaValue);
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

    public void unAssignVisitFromShift(Shift shift, int index) {
        List<Visit> route = solution.getRoute(shift);
        Visit removedVisit = solution.unAssignVisitFromShift(shift, index);
        if (removedVisit.completesTask()){
            // The visit includes completing the task
            solution.unAllocateTask(removedVisit.getTask());
        }
        if (removedVisit.getVisitType()  == Constants.VisitType.JOIN_MOTORIZED && !shift.isMotorized() && route.size() > index){
            // You remove a walkers pick-up point. It should no longer drive to the next task
            route.get(index+1).setCoCarPoolerShiftID(null);
            route.get(index+1).setTransportType(Constants.TransportMode.WALK);
        }
        objective.removeVisit(shift, removedVisit);
    }



    public void addVisitsToUnallocatedVisits(Collection<Visit> visits) { solution.addVisitsToUnallocatedVisits(visits);}

    public boolean calculateAndSetObjectiveValuesForSolution(Model model) { return objective.calculateAndSetObjectiveValues(model, solution);}

	public void unAssignVisitsByRouteIndices(Shift removeShift, List<Integer> indices, double bestIntraObjective) {
        List<Integer> indicesSorted = indices.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());  
        for(int index : indicesSorted){
            unAssignVisitByRouteIndex(removeShift, index, 0.0);
        }
        // Only update intra route objective once, when removing multiple shifts. 
        objective.updateIntraRouteObjective(removeShift, bestIntraObjective);
	}

/**
     * Any objective affected will not be updated.
     *
     * @param visit
     * @param startTime
     */
    public void setTimeDependentTaskStartTime(Visit visit, int startTime) {
        solution.setCarpoolSyncedVisitStartTime(visit, startTime);
    }


}
