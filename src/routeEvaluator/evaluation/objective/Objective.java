package routeEvaluator.evaluation.objective;

import java.util.*;

import algorithm.Solution;
import model.Model;
import model.Shift;
import model.Task;
import routeEvaluator.results.RouteEvaluatorResult;
import routeEvaluator.solver.RouteEvaluator;

/**
 * The objective class holds the total objective values and the objective values for the individual shifts.
 * It is used to calculate objective values using the objective functions. It uses the route evaluator to evaluate
 * objectives that are considered to be intra route objectives and are added to the list of objective function in
 * the route evaluator. The objective functions in the objective class are applied to the list of tasks and are
 * not dependent on distance, time the tasks are executed etc.. Hence it is assumed that these objectives can be
 * evaluated just by being allocated to the route or not.
 * The objective value for a shift, and in turn for the entire solution is then the sum of the objectives in the
 * route evaluator and the ones in the objective class.
 */
public class Objective {

    private Map<Integer, RouteEvaluator> routeEvaluators;
    private final Map<String, WeightObjectivePair<IObjectiveFunction>> activeObjectiveFunctions;
    private final Map<String, WeightObjectivePair<IObjectiveFunction>> inActiveObjectiveFunctions;
    private double[] shiftIntraRouteValues;
    private double[] shiftTotalRouteValues;
    private double totalObjectiveValue;
    private final List<Task> addRemoveTasks;

    public void deActivateExtraObjectiveFunction(String name) {
        if (!activeObjectiveFunctions.containsKey(name))
            return;
        WeightObjectivePair<IObjectiveFunction> weightObjectivePair = activeObjectiveFunctions.remove(name);
        inActiveObjectiveFunctions.put(name, weightObjectivePair);
    }


    public void updateObjectiveWeight(String name, double newWeight) {
        WeightObjectivePair<IObjectiveFunction> obj = activeObjectiveFunctions.getOrDefault(name, inActiveObjectiveFunctions.get(name));
        obj.setWeight(newWeight);
    }

    public void activateExtraObjectiveFunction(String name, Solution solution) {
        WeightObjectivePair<IObjectiveFunction> weightObjectivePair = inActiveObjectiveFunctions.remove(name);
        if (weightObjectivePair != null) {
            activeObjectiveFunctions.put(name, weightObjectivePair);
        } else {
            weightObjectivePair = activeObjectiveFunctions.get(name);
        }
        weightObjectivePair.getObjectiveFunction().updateState(solution);
    }

    public void activateExtraObjectiveFunction(String name, WeightObjectivePair<IObjectiveFunction> weightObjectivePairOther) {
        WeightObjectivePair<IObjectiveFunction> weightObjectivePair = inActiveObjectiveFunctions.remove(name);
        weightObjectivePair.getObjectiveFunction().update(weightObjectivePairOther.getObjectiveFunction());
        activeObjectiveFunctions.put(name, weightObjectivePair);
    }

    private Objective() {
        this.activeObjectiveFunctions = new LinkedHashMap<>();
        this.inActiveObjectiveFunctions = new LinkedHashMap<>();
        this.addRemoveTasks = new ArrayList<>();
    }

    public Objective(Model model) {
        this();
        this.shiftIntraRouteValues = new double[model.getShifts().size()];
        this.shiftTotalRouteValues = new double[model.getShifts().size()];
        this.routeEvaluators = initializeRouteEvaluators(model);
    }

    public Objective(Model model, Map<Integer, RouteEvaluator> routeEvaluators) {
        this();
        this.shiftIntraRouteValues = new double[model.getShifts().size()];
        this.shiftTotalRouteValues = new double[model.getShifts().size()];
        this.routeEvaluators = routeEvaluators;
    }

    public Map<Integer, RouteEvaluator> initializeRouteEvaluators(Model model) {
        var evaluators = new LinkedHashMap<Integer, RouteEvaluator>();
        for (Shift shift : model.getShifts()) {
            RouteEvaluator routeEvaluator = new RouteEvaluator(model.getTravelTimeMatrix(), model.getVisits(), model.getOriginLocation());
            //updateRouteEvaluatorLocations(shift, routeEvaluator);
            evaluators.put(shift.getId(), routeEvaluator);
        }
        return evaluators;
    }

    // Should not be the case, since all our routes starts in origin
    /* public static void updateRouteEvaluatorLocations(Shift shift, RouteEvaluator routeEvaluator) {
        if (shift.getStartLocation() == null) routeEvaluator.useOpenStartRoutes();
        else routeEvaluator.updateOrigin(shift.getStartLocation());
        if (shift.getEndLocation() == null) routeEvaluator.useOpenEndedRoutes();
        else routeEvaluator.updateDestination(shift.getEndLocation());
    }
    */
    public Objective(Objective objective) {
        this.shiftIntraRouteValues = Arrays.copyOf(objective.shiftIntraRouteValues, objective.shiftIntraRouteValues.length);
        this.shiftTotalRouteValues = Arrays.copyOf(objective.shiftTotalRouteValues, objective.shiftTotalRouteValues.length);
        this.totalObjectiveValue = objective.totalObjectiveValue;
        this.routeEvaluators = makeRouteEvaluatorCopy(objective.routeEvaluators);
        this.activeObjectiveFunctions = new LinkedHashMap<>();
        this.inActiveObjectiveFunctions = new LinkedHashMap<>();
        this.addRemoveTasks = new ArrayList<>();
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
        System.arraycopy(other.shiftTotalRouteValues, 0, this.shiftTotalRouteValues, 0, this.shiftIntraRouteValues.length);

        for (var kvp : other.activeObjectiveFunctions.entrySet()) {
            if (this.activeObjectiveFunctions.containsKey(kvp.getKey()))
                this.activeObjectiveFunctions.get(kvp.getKey()).getObjectiveFunction().update(kvp.getValue().getObjectiveFunction());
            else {
                this.activateExtraObjectiveFunction(kvp.getKey(), kvp.getValue());
            }
        }
        for (var kvp : other.inActiveObjectiveFunctions.entrySet()) {
            if (this.inActiveObjectiveFunctions.containsKey(kvp.getKey()))
                this.inActiveObjectiveFunctions.get(kvp.getKey()).getObjectiveFunction().update(kvp.getValue().getObjectiveFunction());
            else {
                this.deActivateExtraObjectiveFunction(kvp.getKey());
            }
        }
    }

    protected void addTask(Shift shift, Task task) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().addingTask(shift, task);
        }
    }

    protected void addTasks(Shift shift, Collection<Task> tasks) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().addingTasks(shift, tasks);
        }
    }

    protected void removeTasks(Shift shift, Collection<Task> tasks) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().removingTasks(shift, tasks);
        }
    }

    protected void removeTask(Shift shift, Task task) {
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectivePair.getObjectiveFunction().removingTask(shift, task);
        }
    }

    protected void removeTasks(Shift shift, List<Task> currentRoute, List<Integer> removeTasks) {
        addRemoveTasks.clear();
        for (int taskIndex : removeTasks)
            addRemoveTasks.add(currentRoute.get(taskIndex));
        removeTasks(shift, addRemoveTasks);
    }

    /**
     * Calculates the delta objective value for a route when removing a task at a specific index in a route.
     * The delta value is compared to the currently stored objective value for the shift.
     *
     * @param shift                Shift to calculate objective value for.
     * @param route                Route to evaluate.
     * @param index                Index to remove.
     * @param syncedTasksStartTime Start time for synced tasks.
     * @return Change in objective value or null if the change is infeasible.
     */
    public Double deltaIntraObjectiveRemovingTaskAtIndex(Shift shift, List<Task> route, int index, Map<ITask, Integer> syncedTasksStartTime) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        Double newObj = routeEvaluator.evaluateRouteByTheOrderOfTasksRemoveTaskObjective(route, index, syncedTasksStartTime, shift);
        if (newObj == null)
            return null;
        return newObj - shiftIntraRouteValues[shift.getId()];
    }

    /**
     * @param shift                Shift to calculate objective value for.
     * @param removeIndicesInRoute Skips tasks at these indices in the route, e.g., if the set contains index 0 and 2.
     *                             task number 0 and 2 in the route will not be visited.
     * @param syncedTasksStartTime Start time for synced tasks.
     * @return Delta objective or null if infeasible
     */
    public Double deltaIntraObjectiveNewRoute(Shift shift, List<Task> route, List<Integer> removeIndicesInRoute, Map<ITask, Integer> syncedTasksStartTime) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        Double newObj = routeEvaluator.evaluateRouteByTheOrderOfTasksRemoveTaskObjective(route, removeIndicesInRoute, syncedTasksStartTime, shift);
        if (newObj == null)
            return null;
        return newObj - shiftIntraRouteValues[shift.getId()];
    }

    public RouteEvaluatorResult<Task> routeEvaluatorResult(Shift shift, List<Task> route, Map<ITask, Integer> syncedTasksStartTime) {
        RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
        return routeEvaluator.evaluateRouteByTheOrderOfTasks(route, syncedTasksStartTime, shift);
    }


    /**
     * Adds an objective function to the objective. The given weight is the weight used
     * in the weighted objective.
     *
     * @param name              Identifier of the objective.
     * @param objectiveFunction Objective function to add.
     * @param weight            Weight objective.
     */
    protected void addExtraRouteObjectiveFunction(String name, IObjectiveFunction objectiveFunction, double weight) {
        activeObjectiveFunctions.put(name, new WeightObjectivePair<>(weight, objectiveFunction));
    }

    /**
     * Calculate the objective value of the route for a given shift.
     * Returns the individual un weighted values for each objective.
     *
     * @param shift Shift to calculate objective for.
     * @param tasks Tasks in the route.
     * @return Individual unweighted objective values.
     */
    public Map<String, Double> calcExtraRouteObjectiveValues(Shift shift, List<Task> tasks) {
        Map<String, Double> objectiveValues = new HashMap<>();
        for (Map.Entry<String, WeightObjectivePair<IObjectiveFunction>> nameWeightObjectivePair : activeObjectiveFunctions.entrySet()) {
            objectiveValues.put(nameWeightObjectivePair.getKey(),
                    nameWeightObjectivePair.getValue().getObjectiveFunction().calculateObjectiveValueFor(shift, tasks));
        }
        return objectiveValues;
    }

    /**
     * Calculate the objective value of the route for a given shift.
     *
     * @param shift Shift to calculate objective for.
     * @param tasks Tasks in the route.
     * @return Weighted total objective.
     */
    public double calcExtraRouteObjectiveValue(Shift shift, List<Task> tasks) {
        double objectiveValue = 0;
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectiveValue += objectivePair.getWeight() * objectivePair.getObjectiveFunction().calculateObjectiveValueFor(shift, tasks);
        }
        return objectiveValue;
    }

    /**
     * Calculate the delta objective value when removing a task from a route for a given shift.
     *
     * @param shift      Shift to calculate objective for.
     * @param removeTask Task in the current that should be removed.
     * @return Weighted delta objective for the shift.
     */
    public double deltaExtraRouteObjectiveValueRemove(Shift shift, Task removeTask) {
        double objectiveValue = 0;
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectiveValue += objectivePair.getWeight() * objectivePair.getObjectiveFunction().calculateDeltaObjectiveValueRemovingTask(shift, removeTask);
        }
        return objectiveValue;
    }

    /**
     * Calculate the delta objective value when removing a task from a route for a given shift.
     *
     * @param shift        Shift to calculate objective for.
     * @param currentRoute Tasks in the current route.
     * @param removeTasks  Task in the current that should be removed, given as indices (sorted) in the list of @param currentTasks.
     * @return Weighted delta objective for the shift.
     */
    public double deltaExtraRouteObjectiveValueRemove(Shift shift, List<Task> currentRoute, List<Integer> removeTasks) {
        double objectiveValue = 0;
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectiveValue += objectivePair.getWeight() * objectivePair.getObjectiveFunction().calculateDeltaObjectiveValueRemovingTasks(shift, currentRoute, removeTasks);
        }
        return objectiveValue;
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
     * Calculate the delta objective value when adding a task to a route for a given shift.
     *
     * @param shift      Shift to calculate objective for.
     * @param insertTask Task to add.
     * @return Weighted delta objective for the shift.
     */
    public double calcDeltaExtraRouteObjectiveValueInsert(Shift shift, Task insertTask) {
        double objectiveValue = 0;
        for (WeightObjectivePair<IObjectiveFunction> objectivePair : activeObjectiveFunctions.values()) {
            objectiveValue += objectivePair.getWeight() *
                    objectivePair.getObjectiveFunction().calculateDeltaObjectiveValueAddingTask(shift, insertTask);
        }
        return objectiveValue;
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
        return shiftTotalRouteValues[shift.getId()];
    }

    /**
     * Extra Objective values for the entire shift.
     *
     * @param shift Shift to get objective value for.
     * @return Objective value.
     */
    public double getShiftExtraObjectiveValue(Shift shift) {
        return shiftTotalRouteValues[shift.getId()] - shiftIntraRouteValues[shift.getId()];
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
        shiftTotalRouteValues[shift.getId()] += deltaValue;
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
        shiftTotalRouteValues[shift.getId()] += deltaValue;
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
        double[] tmpShiftIntraRouteValues = new double[model.getShifts().size()];
        double[] tmpShiftTotalRouteValues = new double[model.getShifts().size()];

        for (Shift shift : model.getShifts()) {
            RouteEvaluator routeEvaluator = routeEvaluators.get(shift.getId());
            Double intraObjective = routeEvaluator.evaluateRouteObjective(solution.getRoute(shift), solution.getSyncedTaskStartTimes(), shift);
            if (intraObjective == null)
                return false;
            double extraObjective = calcExtraRouteObjectiveValue(shift, solution.getRoute(shift));
            tmpShiftIntraRouteValues[shift.getId()] = intraObjective;
            tmpShiftTotalRouteValues[shift.getId()] = intraObjective + extraObjective;
            tmpTotalObjectiveValue += intraObjective + extraObjective;
        }

        totalObjectiveValue = tmpTotalObjectiveValue;
        System.arraycopy(tmpShiftIntraRouteValues, 0, shiftIntraRouteValues, 0, shiftIntraRouteValues.length);
        System.arraycopy(tmpShiftTotalRouteValues, 0, shiftTotalRouteValues, 0, shiftTotalRouteValues.length);

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

    public List<IObjectiveFunction> extractExtraObjectiveFunctions() {
        List<IObjectiveFunction> extraObjectiveFunctions = new ArrayList<>();
        for (WeightObjectivePair<IObjectiveFunction> weightObjectivePair : activeObjectiveFunctions.values())
            extraObjectiveFunctions.add(weightObjectivePair.getObjectiveFunction());
        return extraObjectiveFunctions;
    }
}

