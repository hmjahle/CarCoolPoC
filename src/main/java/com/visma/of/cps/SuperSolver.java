package com.visma.of.cps;

import com.visma.of.cps.algorithm.LargeNeighborhoodSearch;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.OvertimeIntraRouteConstraint;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.StrictTimeWindowConstraint;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.SyncedTasksConstraint;
import com.visma.of.cps.routeEvaluator.evaluation.objective.*;
import com.visma.of.cps.routeEvaluator.solver.RouteEvaluator;
import com.visma.of.cps.solution.Problem;
import com.visma.of.cps.util.Constants;
import com.visma.of.cps.util.Constants.Penalty;

import java.util.Map;



public class SuperSolver {

    private Model model;
    public String name;
    private Problem currentBestSolution;
    private LargeNeighborhoodSearch lns;

    public SuperSolver() {
    }
    public void initialize(int modelInstance) {
        this.model = new Model(4);
        model.initialize();
    }

    public void initialize(Model model) {
        this.model = model;
        model.loadData();
    }

    public void solve() {
        System.out.println("Solver running!");
        lns = new LargeNeighborhoodSearch(model);
        var problem = initializeLNS(model, lns);
        // Orginally: call newBestSolutionFound to update listeners
        Problem newBestSolution = lns.solveWithConstructionHeuristic(problem);
        this.currentBestSolution = newBestSolution;
    }


    public static Problem initializeLNS(Model model, LargeNeighborhoodSearch lns) {
        lns.setUnallocatedTasksAreHierarchical(true);
        Problem problem = new Problem(model);
        problem.addVisitsToUnallocatedVisits(model.getVisits());
        problem.addTasksToUnallocatedTasks(model.getTasks());

        lns.initializeStandardOperators();

        initializeStandardIntraRouteObjectives(model, problem);

        initializeRelaxedIntraRouteConstraints(model, problem);

        problem.calculateAndSetObjectiveValuesForSolution(model);
        return problem;
    }

    public static void initializeRelaxedIntraRouteConstraints(Model model, Problem problem) {
        problem.addRelaxedIntraConstraint(OvertimeIntraRouteConstraint.class.getSimpleName(), new OvertimeIntraRouteConstraint(model), new OvertimeObjectiveFunction());
        problem.addRelaxedIntraConstraint(StrictTimeWindowConstraint.class.getSimpleName(), new StrictTimeWindowConstraint(), new StrictTimeWindowObjectiveFunction(Penalty.STRICT_TIME_WINDOW_RELAXATION_PENALTY_DEFAULT));
        problem.addRelaxedIntraConstraint(SyncedTasksConstraint.class.getSimpleName(), new SyncedTasksConstraint(Constants.SYNCED_TASK_CONSTRAINT_ALLOWED_SLACK_DEFAULT), new SyncedVisitStartTimeObjective(Constants.SYNCED_TASK_CONSTRAINT_ALLOWED_SLACK_DEFAULT, 15.0));
    
    }

    /**
     * Initialize and add the standard intra route objectives.
     */
    public static void initializeStandardIntraRouteObjectives(Model model, Problem problem) {
        TravelTimeObjectiveFunction travelTimeObjectiveFunction = new TravelTimeObjectiveFunction();
        TimeWindowLowHighObjectiveFunction timeWindowObjectiveFunction = new TimeWindowLowHighObjectiveFunction(300, 3);

        // Iterate through the routeevaluators (number of shifts in the model) stored in the objective in the problem. problem.objective.routeevaluators
        for (Map.Entry<Integer, RouteEvaluator> shiftIdRouteEvaluator : problem.getRouteEvaluators().entrySet()) {
            shiftIdRouteEvaluator.getValue().addObjectiveIntraShift(TravelTimeObjectiveFunction.class.getSimpleName(), Constants.TRAVEL_TIME_WEIGTH, travelTimeObjectiveFunction);
            shiftIdRouteEvaluator.getValue().addObjectiveIntraShift(TimeWindowLowHighObjectiveFunction.class.getSimpleName(),Constants.TIME_WINDOW_WEIGHT, timeWindowObjectiveFunction);
        }
    }

    public LargeNeighborhoodSearch getLns() {
        return lns;
    }

    public Problem getCurrentBestSolution() {
        return currentBestSolution;
    }

    public Model getModel() {
        return model;
    }


}

