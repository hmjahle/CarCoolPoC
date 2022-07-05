package routeEvaluator.evaluation.objective;

import java.util.*;

import algorithm.Solution;
import model.Model;
import model.Shift;
import model.Visit;
import routeEvaluator.results.RouteEvaluatorResult;
import routeEvaluator.solver.RouteEvaluator;

/**
 * The objective class holds the total objective values and the objective values for the individual shifts.
 * It is used to calculate objective values using the objective functions. It uses the route evaluator to evaluate
 * objectives that are considered to be intra route objectives and are added to the list of objective function in
 * the route evaluator. The objective functions in the objective class are applied to the list of visits and are
 * not dependent on distance, time the visits are executed etc.. Hence it is assumed that these objectives can be
 * evaluated just by being allocated to the route or not.
 * The objective value for a shift, and in turn for the entire solution is then the sum of the objectives in the
 * route evaluator and the ones in the objective class.
 */
public class Objective {

    private Map<Integer, RouteEvaluator> routeEvaluators;
    private final Map<String, WeightObjectivePair<IObjectiveFunction>> activeObjectiveFunctions;
    private final Map<String, WeightObjectivePair<IObjectiveFunction>> inActiveObjectiveFunctions;
    private double[] shiftIntraRouteValues;
    //private double[] shiftTotalRouteValues;
    private double totalObjectiveValue;
    private final List<Visit> addRemoveVisits;


    public void updateObjectiveWeight(String name, double newWeight) {
        WeightObjectivePair<IObjectiveFunction> obj = activeObjectiveFunctions.getOrDefault(name, inActiveObjectiveFunctions.get(name));
        obj.setWeight(newWeight);
    }

    private Objective() {
        this.activeObjectiveFunctions = new LinkedHashMap<>();
        this.inActiveObjectiveFunctions = new LinkedHashMap<>();
        this.addRemoveVisits = new ArrayList<>();
    }

    public Objective(Model model) {
        this();
        this.shiftIntraRouteValues = new double[model.getShifts().size()];
        this.routeEvaluators = initializeRouteEvaluators(model);
    }

    public Objective(Model model, Map<Integer, RouteEvaluator> routeEvaluators) {
        this();
        this.shiftIntraRouteValues = new double[model.getShifts().size()];
        this.routeEvaluators = routeEvaluators;
    }

    public Map<Integer, RouteEvaluator> initializeRouteEvaluators(Model model) {
        var evaluators = new LinkedHashMap<Integer, RouteEvaluator>();
        for (Shift shift : model.getShifts()) {
            // Use constructor that sets origin and destination to depot
            RouteEvaluator routeEvaluator = new RouteEvaluator(model.getTravelTimeMatrix(), model.getVisits(), model.getOriginLocation());
            evaluators.put(shift.getId(), routeEvaluator);
        }
        return evaluators;
    }

    public Objective(Objective objective) {
        this.shiftIntraRouteValues = Arrays.copyOf(objective.shiftIntraRouteValues, objective.shiftIntraRouteValues.length);
        this.totalObjectiveValue = objective.totalObjectiveValue;
        this.routeEvaluators = makeRouteEvaluatorCopy(objective.routeEvaluators);
        this.activeObjectiveFunctions = new LinkedHashMap<>();
        this.inActiveObjectiveFunctions = new LinkedHashMap<>();
        this.addRemoveVisits = new ArrayList<>();
        for (Map.Entry<String, WeightObjectivePair<IObjectiveFunction>> kvp : objective.activeObjectiveFunctions.entrySet()) {
            activeObjectiveFunctions.put(kvp.getKey(),
                    new WeightObjectivePair<>(kvp.getValue().getWeight(), kvp.getValue().getObjectiveFunction().copy()));
        }
        for (Map.Entry<String, WeightObjectivePair<IObjectiveFunction>> kvp : objective.inActiveObjectiveFunctions.entrySet()) {
            inActiveObjectiveFunctions.put(kvp.getKey(),
                    new WeightObjectivePair<>(kvp.getValue().getWeight(), kvp.getValue().getObjectiveFunction().copy()));
        }
    }

    private Map<Integer, RouteEvaluator> makeRouteEvaluatorCopy(Map<Integer, RouteEvaluator> routeEvaluators) {
        if (routeEvaluators == null) return null;
        return new LinkedHashMap<>(routeEvaluators);
    }

    private void updateRouteEvaluators(Map<Integer, RouteEvaluator> routeEvaluatorsMapping) {
        for (var entry : routeEvaluatorsMapping.entrySet()) {
            this.routeEvaluators.get(entry.getKey()).update(entry.getValue());
        }
    }

    protected void update(Objective other) {
        updateRouteEvaluators(other.routeEvaluators);
        this.totalObjectiveValue = other.totalObjectiveValue;
        System.arraycopy(other.shiftIntraRouteValues, 0, this.shiftIntraRouteValues, 0, this.shiftIntraRouteValues.length);

        for (var kvp : other.activeObjectiveFunctions.entrySet()) {
            this.activeObjectiveFunctions.get(kvp.getKey()).getObjectiveFunction().update(kvp.getValue().getObjectiveFunction());
        }
        for (var kvp : other.inActiveObjectiveFunctions.entrySet()) {
            this.inActiveObjectiveFunctions.get(kvp.getKey()).getObjectiveFunction().update(kvp.getValue().getObjectiveFunction());
        }
    }

    protected void addVisit(Shift shift, Visit visit) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().addingVisit(shift, visit);
        }
    }

    protected void addVisits(Shift shift, Collection<Visit> visits) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().addingVisits(shift, visits);
        }
    }

    protected void removeVisits(Shift shift, Collection<Visit> visits) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().addingVisits(shift, visits);
        }
    }

    protected void removeVisit(Shift shift, Visit visit) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().removingVisit(shift, visit);
        }
    }

    protected void removeVisits(Shift shift, List<Visit> currentRoute, List<Integer> removeVisits) {
        addRemoveVisits.clear();
        for (int taskIndex : removeVisits)
            addRemoveVisits.add(currentRoute.get(taskIndex));
        removeVisits(shift, addRemoveVisits);
    }

    /**
     * Calculates the delta objective value for a route when removing a visit at a specific index in a route.
     * The delta value is compared to the currently stored objective value for the shift.
     *
     * @param shift                Shift to calculate objective value for.
     * @param route                Route to evaluate.
     * @param index                Index to remove.
     * @param syncedVisitsStartTime Start time for synced visits.
     * @return Change in objective value or null if the change is infeasible.
     */
    public Double deltaIntraObjectiveRemovingVisitAtIndex(Shift shift, List<Visit> route, int index) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        Double newObj = routeEvaluator.evaluateRouteByTheOrderOfVisitsRemoveVisitObjective(route, index, shift);
        if (newObj == null)
            return null;
        return newObj - shiftIntraRouteValues[shift.getId()];
    }

    /**
     * @param shift                Shift to calculate objective value for.
     * @param removeIndicesInRoute Skips visits at these indices in the route, e.g., if the set contains index 0 and 2.
     *                             visit number 0 and 2 in the route will not be visited.
     * @param syncedVisitsStartTime Start time for synced visits.
     * @return Delta objective or null if infeasible
     */
    public Double deltaIntraObjectiveNewRoute(Shift shift, List<Visit> route, List<Integer> removeIndicesInRoute) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        Double newObj = routeEvaluator.evaluateRouteByTheOrderOfVisitsRemoveVisitObjective(route, removeIndicesInRoute, shift);
        if (newObj == null)
            return null;
        return newObj - shiftIntraRouteValues[shift.getId()];
    }

    public RouteEvaluatorResult routeEvaluatorResult(Shift shift, List<Visit> route) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        return routeEvaluator.evaluateRouteByTheOrderOfVisits(route, shift);
    }

    /**
     * Set the intra route objective value for a given shift.
     *
     * @param shift          Shift to set objective value for.
     * @param objectiveValue Objective value.
     */
    protected void setShiftIntraRouteObjectiveValue(Shift shift, double objectiveValue) {
        shiftIntraRouteValues[shift.getId()] = objectiveValue;
    }

    /**
     * Objective value for the entire solution.
     *
     * @return Objective value.
     */
    public double getTotalObjectiveValue() {
        return totalObjectiveValue;
    }

    public double getShiftIntraRouteObjectiveValue(Shift shift) {
        return shiftIntraRouteValues[shift.getId()];
    }

    /**
     * Objective value for the entire shift.
     *
     * @param shift Shift to get objective value for.
     * @return Objective value.
     */
    public double getShiftTotalObjectiveValue(Shift shift) {
        return shiftIntraRouteValues[shift.getId()];
    }

    /**
     * Updates the objective value by adding the delta value to the objective of the shift and
     * the total objective value.
     *
     * @param shift      Shift where the objective should be adjusted.
     * @param deltaValue Double value to add to the objective.
     */
    protected void updateTotalRouteObjective(Shift shift, double deltaValue) {
        totalObjectiveValue += deltaValue;
        shiftIntraRouteValues[shift.getId()] += deltaValue;
    }


    /**
     * Updates the intra route objective value by adding the delta value to the objective of the shift and
     * the total objective value.
     *
     * @param shift      Shift where the objective should be adjusted.
     * @param deltaValue Double value to add to the objective.
     */
    protected void updateIntraRouteObjective(Shift shift, double deltaValue) {
        totalObjectiveValue += deltaValue;
        shiftIntraRouteValues[shift.getId()] += deltaValue;
    }

    /**
     * Calculates the objective value for each shift in the solution and sets the objective values for each shift and
     * the total objective value.
     *
     * @param model    Model that the objective belongs to.
     * @param solution Solution that the objective is calculated for.
     */
    public boolean calculateAndSetObjectiveValues(Model model, Solution solution) {
        double tmpTotalObjectiveValue = 0;
        //double[] tmpShiftIntraRouteValues = new double[model.getShifts().size()];
        double[] tmpShiftTotalRouteValues = new double[model.getShifts().size()];

        for (Shift shift : model.getShifts()) {
            RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
            Double intraObjective = routeEvaluator.evaluateRouteObjective(solution.getRoute(shift), shift);
            if (intraObjective == null)
                return false;
            // double extraObjective = calcExtraRouteObjectiveValue(shift, solution.getRoute(shift));
            //tmpShiftIntraRouteValues[shift.getId()] = intraObjective;
            tmpShiftTotalRouteValues[shift.getId()] = intraObjective;
            tmpTotalObjectiveValue += intraObjective;
        }

        totalObjectiveValue = tmpTotalObjectiveValue;
        //System.arraycopy(tmpShiftIntraRouteValues, 0, shiftIntraRouteValues, 0, shiftIntraRouteValues.length);
        System.arraycopy(tmpShiftTotalRouteValues, 0, shiftIntraRouteValues, 0, shiftIntraRouteValues.length);

        return true;
    }

    public Map<String, WeightObjectivePair<IObjectiveFunction>> getActiveObjectiveFunctions() {
        return activeObjectiveFunctions;
    }

    /**
     * @return Mapping from shiftId to RouteEvaluator
     */
    public Map<Integer, RouteEvaluator> getRouteEvaluators() {
        return routeEvaluators;
    }
}

