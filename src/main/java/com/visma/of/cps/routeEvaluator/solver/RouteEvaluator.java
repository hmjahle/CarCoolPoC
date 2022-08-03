package com.visma.of.cps.routeEvaluator.solver;

import com.visma.of.cps.model.Location;
import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.TravelTimeMatrix;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.ConstraintsIntraRouteHandler;
import com.visma.of.cps.routeEvaluator.evaluation.constraint.IConstraintIntraRoute;
import com.visma.of.cps.routeEvaluator.evaluation.objective.IObjectiveFunctionIntraRoute;
import com.visma.of.cps.routeEvaluator.evaluation.objective.ObjectiveFunctionsIntraRouteHandler;
import com.visma.of.cps.routeEvaluator.evaluation.objective.WeightedObjective;
import com.visma.of.cps.routeEvaluator.results.RouteEvaluatorResult;
import com.visma.of.cps.routeEvaluator.solver.algorithm.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The route evaluator calculates the fitness of a route.
 * When it is used it will only evaluate fitness' and hard constraints,
 * that relates to things that happens within a route. Hence fitness' like Visit history, work balance,
 * heavy visits, etc. is not evaluated.
 * W.r.t. hard constraints the same assumption applies. Hence constraints like overtime.
 * Max travel distance on bike / walk, avoid overtime is handled within the route evaluator.
 * Where constraints like two incompatible visits on same route and heavy visits will be ignored.
 * It is therefore assumed that input to the evaluator is feasible w.r.t. to these types of constraints.
 */
public class RouteEvaluator {

    private final SearchGraph graph;
    private final ObjectiveFunctionsIntraRouteHandler objectiveFunctions;
    private final ConstraintsIntraRouteHandler constraints;
    private final LabellingAlgorithm algorithm;
    private final NodeList firstNodeList;
    private final NodeList secondNodeList;
    private final int[] syncedNodesStartTime;

    // Remove, since origing and destination always has to de depot
    /* public RouteEvaluator(Map<Integer, TravelTimeMatrix>  distanceMatrixMatrix, Collection<Visit> visits) {
        this(distanceMatrixMatrix, visits, null, null);
    } */

    public RouteEvaluator(Map<Integer, TravelTimeMatrix>  distanceMatrixMatrix, Collection<Visit> visits, Location officePosition) {
        this(distanceMatrixMatrix, visits, officePosition, officePosition);
    }

    public RouteEvaluator(RouteEvaluator other) {
        this.graph = new SearchGraph(other.graph);
        this.objectiveFunctions = new ObjectiveFunctionsIntraRouteHandler(other.objectiveFunctions);
        this.constraints = new ConstraintsIntraRouteHandler(other.constraints);
        this.algorithm = new LabellingAlgorithm(graph, objectiveFunctions, constraints);
        this.firstNodeList = new NodeList(graph.getNodes().size());
        this.secondNodeList = new NodeList(graph.getNodes().size());
        this.syncedNodesStartTime = Arrays.copyOf(other.syncedNodesStartTime, other.syncedNodesStartTime.length);
    }

    public RouteEvaluator(Map<Integer, TravelTimeMatrix>  distanceMatrixMatrix, Collection<Visit> visits,
                          Location origin, Location destination) {
        this.graph = new SearchGraph(distanceMatrixMatrix, visits, origin, destination);
        this.objectiveFunctions = new ObjectiveFunctionsIntraRouteHandler();
        this.constraints = new ConstraintsIntraRouteHandler();
        this.algorithm = new LabellingAlgorithm(graph, objectiveFunctions, constraints);
        this.firstNodeList = new NodeList(graph.getNodes().size());
        this.secondNodeList = new NodeList(graph.getNodes().size());
        this.syncedNodesStartTime = new int[graph.getNodes().size()];
    }

    /**
     * Updates the active and inactive constraints and objectives
     */
    public void update(RouteEvaluator other) {
        this.objectiveFunctions.update(other.objectiveFunctions);
        this.constraints.update(other.constraints);
    }

    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     * Only returns objective value, no route details is returned.
     *
     * @param visits               The route to be evaluated, the order of the list is the order of the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return A double value representing the objective value of the route.
     */
    // Changed to not support synced visits
    public Double evaluateRouteObjective(List<Visit> visits, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        ExtendInfoOneElement nodeExtendInfoOneElement = initializeOneElementEvaluator(visits, syncedVisitsStartTime);

        // Lable is path + cost + time etc
        Label bestLabel = algorithm.
                runAlgorithm(new WeightedObjective(), nodeExtendInfoOneElement, syncedNodesStartTime, employeeWorkShift);
        return bestLabel == null ? null : bestLabel.getObjective().getObjectiveValue();

    }

    /**
     * Evaluates whether the route is feasible when all constraints are activated (including inactive constraints)
     * The route evaluated is given by the visits input, the order of the visits is the order of the route.
     * Only returns whether the route is feasible, no route details is returned.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     * @param employeeWorkShift    Employee the route applies to.
     * @return A bool value representing the feasibility of the route.
     */
    public boolean evaluateRouteFeasibilityForAllConstraints(List<Visit> visits, Map<Visit, Integer> syncedVisitsStartTime, 
                                                             Shift employeeWorkShift) {
        constraints.activateCheckAllActiveAndInactiveConstraints();
        boolean feasible = evaluateRouteObjective(visits, syncedVisitsStartTime, employeeWorkShift) != null;
        constraints.deActivateCheckAllActiveAndInactiveConstraints();
        return feasible;
    }

    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     * It evaluates the route where the task at the indices to be skipped is ignored, i.e., removed from the route.
     * Only returns objective value, no route details is returned.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param skipVisitsAtIndices   The indices where the visits to be removed are placed in the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     * @param employeeWorkShift    Employee the route applies to.
     * @return A double value representing the objective value of the route.
     */
    public Double evaluateRouteByTheOrderOfVisitsRemoveVisitObjective(List<Visit> visits, List<Integer> skipVisitsAtIndices, 
                                                                      Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcObjectiveRemoveVisit(visits, skipVisitsAtIndices, syncedVisitsStartTime, employeeWorkShift);
    }


    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     * It evaluates the route where the task at the index to be skipped is ignored.
     * Only returns objective value, no route details is returned.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param skipVisitAtIndex      The the index where the task to be removed is placed in the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     * @param employeeWorkShift    Employee the route applies to.
     * @return A double value representing the objective value of the route.
     */
    public Double evaluateRouteByTheOrderOfVisitsRemoveVisitObjective(List<Visit> visits, int skipVisitAtIndex, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcObjectiveRemoveVisit(visits, skipVisitAtIndex, syncedVisitsStartTime, employeeWorkShift);
    }


    /**
     * Used to initialize the route evaluator when
     */
    private ExtendInfoOneElement initializeOneElementEvaluator(List<Visit> visits, Map<Visit, Integer> syncedVisitsStartTime) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        updateFirstNodeList(visits);
        return new ExtendInfoOneElement(firstNodeList);
    }

    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return A routeEvaluator result for the evaluated route.
     */
    public RouteEvaluatorResult evaluateRouteByTheOrderOfVisits(List<Visit> visits, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcRouteEvaluatorResult(new WeightedObjective(), visits, syncedVisitsStartTime, employeeWorkShift);
    }


    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     * Returns an objective that also contains the individual objective values for the different objective
     * functions in the route evaluator.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return A routeEvaluator result for the evaluated route.
     */
    /* public RouteEvaluatorResult evaluateRouteByOrderOfVisitsWithObjectiveValues(List<Visit> visits,
                                                                                  Map<Visit, Integer> syncedVisitsStartTime,
                                                                                  Shift employeeWorkShift) {
        return calcRouteEvaluatorResult(new WeightedObjectiveWithValues(), visits, employeeWorkShift);
    } */

    /**
     * Evaluates the route given by the visits input, the order of the visits is the order of the route.
     * At the same time it finds the optimal position in the route to insert the new task.
     * For routes with no synced visits, the new task to be inserted cannot be synced either.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param insertVisit           The task to be inserted into the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return A routeEvaluator result for the evaluated route.
     */
    public RouteEvaluatorResult evaluateRouteByTheOrderOfVisitsInsertVisit(List<Visit> visits, Visit insertVisit, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcRouteEvaluatorResult(new WeightedObjective(), visits, insertVisit, syncedVisitsStartTime, employeeWorkShift);
    }

    /**
     * Used to calculate objective of routes when removing one task
     */
    private Double calcObjectiveRemoveVisit(List<Visit> visits, int skipVisitAtIndex, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        updateFirstNodeList(visits, skipVisitAtIndex);
        ExtendInfoOneElement nodeExtendInfoOneElement = new ExtendInfoOneElement(firstNodeList);
        Label bestLabel = algorithm.
                runAlgorithm(new WeightedObjective(), nodeExtendInfoOneElement, syncedNodesStartTime, employeeWorkShift);
        return bestLabel == null ? null : bestLabel.getObjective().getObjectiveValue();
    }

    /**
     * Used to calculate objective of routes when removing multiple task
     */
    private Double calcObjectiveRemoveVisit(List<Visit> visits, List<Integer> skipVisitAtIndices, Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        updateFirstNodeList(visits, skipVisitAtIndices);
        ExtendInfoOneElement nodeExtendInfoOneElement = new ExtendInfoOneElement(firstNodeList);
        Label bestLabel = algorithm.
                runAlgorithm(new WeightedObjective(), nodeExtendInfoOneElement, syncedNodesStartTime, employeeWorkShift);
        return bestLabel == null ? null : bestLabel.getObjective().getObjectiveValue();
    }


    /**
     * Evaluates the route given by the visits input, the order of the visits will keep the same order in the final route.
     * This also applies to the visits to insert. However the two lists can be merged in any possible way while adhering
     * to these two conditions.
     * At the same time it finds the optimal position in the route to insert the new visits provided.
     * For routes with no synced visits, the new task to be inserted cannot be synced either.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param insertVisits          The list of visits to be inserted into the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return A routeEvaluator result for the evaluated route.
     */
    public RouteEvaluatorResult evaluateRouteByTheOrderOfVisitsInsertVisits(List<Visit> visits, List<Visit> insertVisits,
                                                                            Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcRouteEvaluatorResult(new WeightedObjective(), visits, insertVisits, syncedVisitsStartTime, employeeWorkShift);
    }


    /**
     * Evaluates the route given by the visits input, the order of the visits will keep the same order in the final route.
     * This also applies to the visits to insert. However the two lists can be merged in any possible way while adhering
     * to these two conditions.
     * At the same time it finds the optimal position in the route to insert the new visits provided.
     * For routes with no synced visits, the new task to be inserted cannot be synced either.
     *
     * @param visits                The route to be evaluated, the order of the list is the order of the route.
     * @param insertVisits          The list of visits to be inserted into the route.
     * @param syncedVisitsStartTime Map of ALL synced visits in the route and their start times. Should not contain visits
     *                             that are not in the route, this will reduce performance
     * @param employeeWorkShift    Employee the route applies to.
     * @return The objective value for the evaluated route or null if infeasible.
     */
    public Double evaluateRouteByTheOrderOfVisitsInsertVisitsObjective(List<Visit> visits, List<Visit> insertVisits,
                                                                       Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        return calcRouteEvaluatorObjective(new WeightedObjective(), visits, insertVisits, syncedVisitsStartTime, employeeWorkShift);
    }

    /**
     * Adds an objective function to the route evaluator.
     *
     * @param objectiveFunctionId The id, must be unique.
     * @param objectiveWeight     Weight of the objective.
     * @param objectiveIntraShift The objective function to be added.
     */
    public void addObjectiveIntraShift(String objectiveFunctionId, double objectiveWeight,
                                       IObjectiveFunctionIntraRoute objectiveIntraShift) {
        objectiveFunctions.addIntraShiftObjectiveFunction(objectiveFunctionId, objectiveWeight, objectiveIntraShift);
    }

    /**
     * Adds an objective function to the route evaluator. With weight one and name equal to the class name.
     *
     * @param objectiveIntraShift The objective function to be added.
     */
    public void addObjectiveIntraShift(IObjectiveFunctionIntraRoute objectiveIntraShift) {
        objectiveFunctions.addIntraShiftObjectiveFunction(
                objectiveIntraShift.getClass().getSimpleName(), 1.0, objectiveIntraShift);
    }

    /**
     * Adds an constraint to the route evaluator.
     *
     * @param constraint The constraint to be added.
     */
    public void addConstraint(IConstraintIntraRoute constraint) {
        constraints.addConstraint(constraint);
    }

    /**
     * Used to calculate routes without inserting new visits.
     */
    private RouteEvaluatorResult calcRouteEvaluatorResult(IRouteEvaluatorObjective objective, List<Visit> visits, 
                                                          Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        ExtendInfoOneElement nodeExtendInfoOneElement = initializeOneElementEvaluator(visits, syncedVisitsStartTime);
        return algorithm.solveRouteEvaluatorResult(objective, nodeExtendInfoOneElement, syncedNodesStartTime, employeeWorkShift);
    }

    /**
     * Used to calculate routes when inserting one new task
     */
    private RouteEvaluatorResult calcRouteEvaluatorResult(IRouteEvaluatorObjective objective, List<Visit> visits, Visit insertVisit, 
                                                          Map<Visit, Integer> syncedVisitsStartTime, Shift employeeWorkShift) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        setSyncedNodesStartTime(syncedVisitsStartTime, insertVisit);
        updateFirstNodeList(visits);
        updateSecondNodeList(insertVisit);
        ExtendInfoTwoElements nodeExtendInfoTwoElements = new ExtendInfoTwoElements(firstNodeList, secondNodeList);
        return algorithm.solveRouteEvaluatorResult(objective, nodeExtendInfoTwoElements, syncedNodesStartTime, employeeWorkShift);
    }

    /**
     * Used to calculate routes when inserting multiple new visits.
     */
    private RouteEvaluatorResult calcRouteEvaluatorResult(IRouteEvaluatorObjective objective, List<Visit> visits, List<Visit> insertVisits,
                                                          Map<Visit, Integer> syncedVisitsStartTime,  Shift employeeWorkShift) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        setSyncedNodesStartTimes(syncedVisitsStartTime, insertVisits);
        updateFirstNodeList(visits);
        updateSecondNodeList(insertVisits);
        ExtendInfoTwoElements nodeExtendInfoTwoElements = new ExtendInfoTwoElements(firstNodeList, secondNodeList);
        return algorithm.solveRouteEvaluatorResult(objective, nodeExtendInfoTwoElements, syncedNodesStartTime, employeeWorkShift);
    }

    /**
     * Used to calculate routes when inserting multiple new visits.
     */
    private Double calcRouteEvaluatorObjective(IRouteEvaluatorObjective objective, List<Visit> visits, List<Visit> insertVisits,
                                               Map<Visit, Integer> syncedVisitsStartTime,  Shift employeeWorkShift) {
        setSyncedNodesStartTimes(syncedVisitsStartTime, visits);
        setSyncedNodesStartTimes(syncedVisitsStartTime, insertVisits);
        updateFirstNodeList(visits);
        updateSecondNodeList(insertVisits);
        ExtendInfoTwoElements nodeExtendInfoTwoElements = new ExtendInfoTwoElements(firstNodeList, secondNodeList);
        return algorithm.solveRouteEvaluatorObjective(objective, nodeExtendInfoTwoElements, syncedNodesStartTime, employeeWorkShift);
    }


    private void updateFirstNodeList(List<Visit> visits) {
        firstNodeList.initializeWithNodes(graph, visits);
    }

    private void updateFirstNodeList(List<Visit> visits, int skipVisitAtIndex) {
        firstNodeList.initializeWithNodes(graph, visits, skipVisitAtIndex);
    }

    private void updateFirstNodeList(List<Visit> visits, List<Integer> skipVisitsAtIndices) {
        firstNodeList.initializeWithNodes(graph, visits, skipVisitsAtIndices);
    }

    private void updateSecondNodeList(Visit task) {
        secondNodeList.initializeWithNode(graph, task);
    }

    private void updateSecondNodeList(List<Visit> visits) {
        secondNodeList.initializeWithNodes(graph, visits);
    }

    private void setSyncedNodesStartTimes(Map<Visit, Integer> syncedVisitsStartTime, Collection<Visit> visits) {
        for (Visit visit : visits)
            setSyncedNodesStartTime(syncedVisitsStartTime, visit);
    }

    private void setSyncedNodesStartTime(Map<Visit, Integer> syncedVisitsStartTime, Visit visit) {
        if (visit.isSynced())
            setStartTime(visit, syncedVisitsStartTime.get(visit));
    }

    public boolean hasObjective(String name) {
        return objectiveFunctions.hasObjective(name);
    }

    private void setStartTime(Visit visit, int startTime) {
        Node node = graph.getNode(visit);
        syncedNodesStartTime[node.getNodeId()] = startTime;
    }

    public ObjectiveFunctionsIntraRouteHandler getObjectiveFunctions() {
        return objectiveFunctions;
    }

    public ConstraintsIntraRouteHandler getConstraints() {
        return constraints;
    }

    /**
     * Activates an inactive constraint
     *
     * @param name Constraint name to be activated.
     * @return True if variable was activated, otherwise false.
     */
    public boolean activateConstraint(String name) {
        return constraints.activateConstraint(name);
    }

    /**
     * Deactivates an active constraint
     *
     * @param name Constraint name to be deactivated.
     * @return True if variable was deactivated, otherwise false.
     */
    public boolean deactivateConstraint(String name) {
        return constraints.deactivateConstraint(name);
    }

    public boolean removeObjectiveIntraShift(String name) {
        return objectiveFunctions.removeObjective(name);
    }
   
}
