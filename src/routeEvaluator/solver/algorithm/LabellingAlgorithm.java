package routeEvaluator.solver.algorithm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import model.Shift;
// import com.visma.of.rp.routeevaluator.results.Route;
// import com.visma.of.rp.routeevaluator.results.RouteEvaluatorResult;
import model.Visit;
import routeEvaluator.evaluation.constraint.ConstraintsIntraRouteHandler;
import routeEvaluator.evaluation.info.ConstraintInfo;
import routeEvaluator.evaluation.objective.ObjectiveFunctionsIntraRouteHandler;
import routeEvaluator.results.Route;
import routeEvaluator.results.RouteEvaluatorResult;
import util.Constants;

/**
 * The labelling algorithm is a resource constrained shortest path algorithm.
 * It finds the minimum cost path from the office through all tasks in the given order.
 */
public class LabellingAlgorithm {

    private final SearchGraph graph;
    private final ObjectiveFunctionsIntraRouteHandler objectiveFunctions;
    private final ConstraintsIntraRouteHandler constraints;
    private final Queue<Label> unExtendedLabels;
    private final Label[] labels;
    private final List<Visit> visits;
    private final LabelLists labelLists;
    private Label bestLabelOnDestination;
    private IExtendInfo nodeExtendInfo;
    private int[] syncedNodesStartTime;
    private Shift employeeWorkShift;

    public LabellingAlgorithm(SearchGraph graph, ObjectiveFunctionsIntraRouteHandler objectiveFunctions, ConstraintsIntraRouteHandler constraints) {
        this.graph = graph;
        this.objectiveFunctions = objectiveFunctions;
        this.constraints = constraints;
        this.labels = new Label[graph.getNodes().size()];
        this.visits = new ArrayList<>();
        for (int i = 0; i < graph.getNodes().size(); i++) // Ensures that the array can hold the maximum potential entries, i.e, all tasks.
            visits.add(null);
        this.labelLists = new LabelLists(graph.getNodes().size());
        this.unExtendedLabels = new PriorityQueue<>(Label::compareTo);
        this.bestLabelOnDestination = null;
    }

    /**
     * Solves the labelling algorithm and returns the objective value of the route.
     *
     * @param nodeExtendInfo       Information on how to extend labels and which resources to use.
     * @param syncedNodesStartTime Intended start time of synced tasks.
     * @param employeeWorkShift    Employee to simulate route for.
     * @param objective            Starting objective.
     * @return Total fitness value, null if infeasible.
     */
    public Label runAlgorithm(IRouteEvaluatorObjective objective, IExtendInfo nodeExtendInfo, int[] syncedNodesStartTime, Shift employeeWorkShift) {
        this.labelLists.clear();
        IResource startResource = nodeExtendInfo.createEmptyResource();
        Label startLabel = createStartLabel(objective, employeeWorkShift.getStartTime(), startResource);
        this.nodeExtendInfo = nodeExtendInfo;
        this.syncedNodesStartTime = syncedNodesStartTime;
        this.employeeWorkShift = employeeWorkShift;
        solveLabellingAlgorithm(startLabel);
        return this.bestLabelOnDestination;
    }

    /**
     * Solves the labelling algorithm and returns the route simulator result.
     *
     * @param nodeExtendInfo       Information on how to extend labels and which resources to use.
     * @param syncedNodesStartTime Intended start time of synced tasks.
     * @param employeeWorkShift    Employee to simulate route for.
     * @param initialObjective     Starting objective.
     * @return RouteEvaluatorResult or null if route is infeasible.
     */
    public RouteEvaluatorResult solveRouteEvaluatorResult(IRouteEvaluatorObjective initialObjective, IExtendInfo nodeExtendInfo, int[] syncedNodesStartTime, Shift employeeWorkShift) {
        Label bestLabel = runAlgorithm(initialObjective, nodeExtendInfo, syncedNodesStartTime, employeeWorkShift);
        if (bestLabel == null)
            return null;
        return buildRouteEvaluatorResult(bestLabel);
    }

    /**
     * Solves the labelling algorithm and returns the route simulator result.
     *
     * @param nodeExtendInfo       Information on how to extend labels and which resources to use.
     * @param syncedNodesStartTime Intended start time of synced tasks.
     * @param employeeWorkShift    Employee to simulate route for.
     * @param initialObjective     Starting objective.
     * @return Objective value or null if route is infeasible.
     */
    public double solveRouteEvaluatorObjective(IRouteEvaluatorObjective initialObjective, IExtendInfo nodeExtendInfo, int[] syncedNodesStartTime, Shift employeeWorkShift) {
        Label bestLabel = runAlgorithm(initialObjective, nodeExtendInfo, syncedNodesStartTime, employeeWorkShift);
        return bestLabel.getObjective().getObjectiveValue();
    }


    /**
     * Extract the solution from the labels and builds the route evaluator results and the visits with the respective information.
     *
     * @param bestLabel Label representing the best route for the employee work shift.
     * @return Results of the route.
     */
    private RouteEvaluatorResult buildRouteEvaluatorResult(Label bestLabel) {
        Route route = new Route();
        route.setRouteFinishedAtTime(bestLabel.getCurrentTime());
        extractVisitsAndSyncedStartTime(bestLabel, route);
        return new RouteEvaluatorResult(bestLabel.getObjective(), route);
    }

    private void solveLabellingAlgorithm(Label startLabel) {
        unExtendedLabels.clear();
        bestLabelOnDestination = null;
        Label currentLabel = startLabel;
        while (currentLabel != null) {
            extendLabelToAllPossibleVisits(currentLabel);
            currentLabel = findNextLabel();
            if (optimalSolutionFound(currentLabel))
                break;
        }
    }

    /**
     * Extends a label to the next node and returns the label to be put on that node.
     *
     * @param thisLabel    The label to be extended.
     * @param extendToInfo The info the contains the node to be extended.
     * @return The label to be placed on the next node.
     */
    public Label extendLabelToNextNode(Label thisLabel, ExtendToInfo extendToInfo) {
        Node nextNode = extendToInfo.getToNode();
        int newLocation = findNewLocation(thisLabel, nextNode); // nextNode.getLocationId();
        Integer travelTime = getTravelTime(thisLabel, nextNode, newLocation);
        // OBS OBS check if travel is possible
        if (!isFeasibleTravleTime(nextNode, travelTime))
            return null;
        /* if (travelTime == null)
            return null; */
        int startOfServiceNextTask = calcStartOfServiceNextTask(thisLabel, nextNode, travelTime, nextNode.isSynced());
        IRouteEvaluatorObjective objective = evaluateFeasibilityAndObjective(thisLabel, nextNode, startOfServiceNextTask, travelTime, nextNode.isSynced(), newLocation);
        if (objective == null)
            return null;
        IResource resources = thisLabel.getResources().extend(extendToInfo);
        return new Label(thisLabel, nextNode, newLocation, objective, resources, startOfServiceNextTask, travelTime);
    }
    
    /**
     * If the transportmode is walking and the walking distance is longer than the maximum walking distance return false
     * @param nextNode
     * @param travelTime
     * @return If the path is allowed to walk or not
     */
    private boolean isFeasibleTravleTime(Node nextNode, int travelTime) {
        // return nextNode.getVisit().getTransportType() == Constants.TransportMode.WALK && travelTime > Constants.MAX_WALK_TIME;
        return true;
    }

    private void extendLabelToAllPossibleVisits(Label label) {
        boolean returnToDestinationNode = true;
        Enumeration<ExtendToInfo> enumerator = nodeExtendInfo.extend(label);
        while (enumerator.hasMoreElements()) {
            returnToDestinationNode = false;
            extendLabel(label, enumerator.nextElement());
        }
        if (returnToDestinationNode) {
            Label newLabel = extendLabelToNextNode(label, new ExtendToInfo(graph.getDestination(), 0));
            if (newLabel != null && (bestLabelOnDestination == null || newLabel.compareTo(bestLabelOnDestination) < 0))
                bestLabelOnDestination = newLabel;
        }
    }

    private void extendLabel(Label label, ExtendToInfo extendToInfo) {
        Label newLabel = extendLabelToNextNode(label, extendToInfo);
        if (newLabel != null && labelLists.addLabelOnNode(newLabel.getNode(), newLabel)) {
            unExtendedLabels.add(newLabel);
        }
    }

    public IRouteEvaluatorObjective extend(IRouteEvaluatorObjective currentObjective, Node toNode, int travelTime, int startOfServiceNextTask, int syncedVisitLatestStartTime) {
        Visit visit = toNode.getVisit();
        int visitEnd = visit != null ? startOfServiceNextTask + visit.getVisitDuration() : 0;
        return objectiveFunctions.calculateObjectiveValue(currentObjective, travelTime, visit,
                startOfServiceNextTask, visitEnd, syncedVisitLatestStartTime, employeeWorkShift);
    }

    private Label findNextLabel() {
        Label currentLabel = unExtendedLabels.poll();
        while (currentLabel != null && currentLabel.isClosed())
            currentLabel = unExtendedLabels.poll();
        return currentLabel;
    }

    private Label createStartLabel(IRouteEvaluatorObjective objective, int startTime, IResource emptyResource) {
        return new Label(null, graph.getOrigin(), graph.getOrigin().getLocationId(),
                objective, emptyResource, startTime, 0);
    }

    private int calcArrivalTimeNextTask(Label thisLabel, int travelTime) {
        int actualTravelTime = travelTime;
        // current time is The start of service of the current task.
        // getCanLeaveLocationAtTime = the start of service time of a task + duration of all tasks performed at that location
        // Traveltime is time from current location to next nodes location
        actualTravelTime = Math.max(travelTime - (thisLabel.getCurrentTime() - thisLabel.getCanLeaveLocationAtTime()), 0);
        return actualTravelTime + thisLabel.getCurrentTime() + thisLabel.getNode().getDurationSeconds();
    }

    private int calcStartOfServiceNextTask(Label thisLabel, Node nextNode, int travelTime, boolean nextNodeIsSynced) {
        int arrivalTimeNextTask = calcArrivalTimeNextTask(thisLabel, travelTime);
        int earliestStartTimeNextTask = findEarliestStartTimeNextTask(nextNode, nextNodeIsSynced);
        return Math.max(arrivalTimeNextTask, earliestStartTimeNextTask);
    }

    private IRouteEvaluatorObjective evaluateFeasibilityAndObjective(Label thisLabel, Node nextNode, int startOfServiceNextTask,
                                                                     int travelTime, boolean nextNodeIsSynced, int newLocation) {
        int syncedTaskLatestStartTime = nextNodeIsSynced ? syncedNodesStartTime[nextNode.getNodeId()] : -1;
        int earliestOfficeReturn = calcEarliestPossibleReturnToOfficeTime(nextNode, newLocation, startOfServiceNextTask);
        int shiftStartTime = thisLabel.getPrevious() == null ? nextNode.getTimeWindowStart() - travelTime : thisLabel.getShiftStartTime();
        ConstraintInfo constraintInfo = new ConstraintInfo(employeeWorkShift, earliestOfficeReturn, nextNode.getVisit(),
                startOfServiceNextTask, syncedTaskLatestStartTime, shiftStartTime);
        if (!constraints.isFeasible(constraintInfo))
            return null;
        return extend(thisLabel.getObjective(), nextNode, travelTime, startOfServiceNextTask, syncedTaskLatestStartTime);
    }

    /**
     * Finds the travel time to the next node. Note that if the current location is (-1) it is at the "origin" and interpreted as the first task will become the origin.
     * Hence, this task will become the origin and therefore the travel time will be 0.
     *
     * @param thisLabel   The current label
     * @param nextNode    Node to extend the label to
     * @param newLocation Location the label is extended to, can be different from the location of the node, e.g., if the
     *                    next does not require physical appearance
     * @return Travel time or null if travel is not possible.
     */
    private int getTravelTime(Label thisLabel, Node nextNode, int newLocation) {
        /* if (thisLabel.getCurrentLocationId() == 0) // When location is depot
            return 0.0; */

        //var locationB = newLocation == thisLabel.getCurrentLocationId() ? newLocation : nextNode.getLocationId();
        return graph.getTravelTime(thisLabel.getCurrentLocationId(), nextNode.getLocationId(), nextNode.getTransportMode());
    }

    private int calcEarliestPossibleReturnToOfficeTime(Node nextNode, Integer newLocation, int startOfServiceNextTask) {
        return startOfServiceNextTask + nextNode.getDurationSeconds() + getTravelTimeToDestination(newLocation);
    }

    private int getTravelTimeToDestination(Integer currentLocation) {
        // We do not implement "Ansattoppgaver", so there will be no tasks at depot
        /* if (currentLocation == 0 || currentLocation == graph.getDestination().getLocationId() || graph.getDestination() instanceof VirtualNode) {
            return 0.0;
        } */
        Integer travelTime = graph.getTravelTime(currentLocation, graph.getDestination().getLocationId(), graph.getDestination().getTransportMode());
        return travelTime == null ? 0 : travelTime;
    }

    private int findNewLocation(Label thisLabel, Node nextNode) {
        return nextNode.getLocationId();
    }

    private int findEarliestStartTimeNextTask(Node nextNode, boolean nextNodeIsSynced) {
        if (nextNodeIsSynced) {
            return syncedNodesStartTime[nextNode.getNodeId()];
        } else {
            return nextNode.getTimeWindowStart();
        }
    }

    private boolean optimalSolutionFound(Label currentLabel) {
        return bestLabelOnDestination != null && currentLabel != null &&
                bestLabelOnDestination.getObjective().getObjectiveValue() < currentLabel.getObjective().getObjectiveValue();
    }

    private void extractVisitsAndSyncedStartTime(Label bestLabel, Route route) {
        int labelCnt = collectLabels(bestLabel);
        int visitCnt = 0;
        for (int i = labelCnt - 1; i > 0; i--) {
            bestLabel = labels[i];
            visitCnt = addVisit(visitCnt, bestLabel);
        }
        route.addVisits(visits.subList(0, visitCnt));
    }

    private int collectLabels(Label currentLabel) {
        int labelCnt = 0;
        while (currentLabel.getPrevious() != null) {
            labels[labelCnt++] = currentLabel;
            currentLabel = currentLabel.getPrevious();
        }
        return labelCnt;
    }

    private int addVisit(int visitCnt, Label currentLabel) {
        Visit visit = currentLabel.getNode().getVisit();
        visit.setTimeWindowStart(currentLabel.getCurrentTime());
        visit.setTravleTime(currentLabel.getTravelTime());
        visits.set(visitCnt++, visit);
        return visitCnt;
    }

    public void setEmployeeWorkShift(Shift employeeWorkShift) {
        this.employeeWorkShift = employeeWorkShift;
    }
}
