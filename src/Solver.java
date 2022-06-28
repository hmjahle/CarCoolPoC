import algorithm.LargeNeighborhoodSearch;
import algorithm.Problem;
import model.Model;
import algorithm.Problem;
import algorithm.LargeNeighborhoodSearch;

public class Solver {

    private Model model;
    public String name;
    private Problem currentBestSoution;

    public Solver() {
    }
    public void intialize(int modelInstance) {
        this.model = new Model(4);
        model.loadData();
    }

    public void solve() {
        System.out.println("Solver running!");
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(model);
        var problem = initializeLNS(model, lns);
        // Orginally: call newBestSolutionFound to update listeners
        Problem newBestSolution = lns.solveWithConstructionHeuristic(problem);
    }


    public static Problem initializeLNS(Model model, LargeNeighborhoodSearch lns) {
        lns.setUnallocatedTasksAreHierarchical(true);
        Problem problem = new Problem(model);
        problem.addTasksToUnallocatedTasks(model.getTasks());

        lns.initializeStandardOperators();

        initializeStandardIntraRouteObjectives(model, problem);

        initializeExtraRouteConstraints(model, problem);
        initializeExtraRouteObjectives(model, problem);

        initializeRelaxedIntraRouteConstraints(model, problem);
        initializeRelaxedExtraRouteConstraints(model, problem);

        problem.calculateAndSetObjectiveValuesForSolution(model);
        return problem;
    }


    /**
     * Initialize and add the standard intra route objectives.
     */
    public static void initializeStandardIntraRouteObjectives(Model model, Problem problem) {
        TravelTimeObjectiveFunction travelTimeObjectiveFunction = new TravelTimeObjectiveFunction();
        TimeWindowLowHighObjectiveFunction timeWindowObjectiveFunction = new TimeWindowLowHighObjectiveFunction(300, 3);
        OvertimeObjectiveFunction overtimeObjectiveFunction = new OvertimeObjectiveFunction();

        // Iterate through the routeevaluators (number of shifts in the model) stored in the objective in the problem. problem.objective.routeevaluators
        for (Map.Entry<Short, RouteEvaluator<Task>> shiftIdRouteEvaluator : problem.getRouteEvaluators().entrySet()) {
            shiftIdRouteEvaluator.getValue().addObjectiveIntraShift(TravelTimeObjectiveFunction.class.getSimpleName(), model.getConfiguration().getTravelTimeWeight(), travelTimeObjectiveFunction);
            shiftIdRouteEvaluator.getValue().addObjectiveIntraShift(TimeWindowObjectiveFunction.class.getSimpleName(), model.getConfiguration().getTimeWindowWeight(), timeWindowObjectiveFunction);
            if (model.getConfiguration().getOvertimeWeight() > 0)
                shiftIdRouteEvaluator.getValue().addObjectiveIntraShift(OvertimeObjectiveFunction.class.getSimpleName(), getOvertimeShiftWeight(model, shiftIdRouteEvaluator.getKey()), overtimeObjectiveFunction);
        }
    }

}

