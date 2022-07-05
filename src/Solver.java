import java.util.Map;

import algorithm.LargeNeighborhoodSearch;
import model.Model;
import model.Task;
import routeEvaluator.solver.RouteEvaluator;
import solution.Problem;
import routeEvaluator.evaluation.constraint.OvertimeIntraRouteConstraint;
import routeEvaluator.evaluation.constraint.StrictTimeWindowConstraint;
import routeEvaluator.evaluation.constraint.TimeWindowLateArrivalConstraint;
import routeEvaluator.evaluation.objective.OvertimeObjectiveFunction;
import routeEvaluator.evaluation.objective.StrictTimeWindowObjectiveFunction;
import routeEvaluator.evaluation.objective.TimeWindowLowHighObjectiveFunction;
import routeEvaluator.evaluation.objective.TravelTimeObjectiveFunction;
import util.Constants.Penalty;
import util.Constants;



public class Solver {

    private Model model;
    public String name;
    private Problem currentBestSolution;
    private LargeNeighborhoodSearch lns;

    public Solver() {
    }
    public void intialize(int modelInstance) {
        this.model = new Model(4);
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

        lns.initializeStandardOperators();

        initializeStandardIntraRouteObjectives(model, problem);

        initializeRelaxedIntraRouteConstraints(model, problem);

        problem.calculateAndSetObjectiveValuesForSolution(model);
        return problem;
    }

    public static void initializeRelaxedIntraRouteConstraints(Model model, Problem problem) {
        problem.addRelaxedIntraConstraint(OvertimeIntraRouteConstraint.class.getSimpleName(), new OvertimeIntraRouteConstraint(model), new OvertimeObjectiveFunction());
        problem.addRelaxedIntraConstraint(StrictTimeWindowConstraint.class.getSimpleName(), new StrictTimeWindowConstraint(), new StrictTimeWindowObjectiveFunction(Penalty.STRICT_TIME_WINDOW_RELAXATION_PENALTY_DEFAULT));
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

