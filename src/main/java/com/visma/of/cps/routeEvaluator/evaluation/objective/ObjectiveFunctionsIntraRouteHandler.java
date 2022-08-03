package com.visma.of.cps.routeEvaluator.evaluation.objective;

//import com.visma.of.rp.routeevaluator.solver.algorithm.IRouteEvaluatorObjective;

import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.routeEvaluator.evaluation.info.ObjectiveInfo;
import com.visma.of.cps.routeEvaluator.solver.algorithm.IRouteEvaluatorObjective;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveFunctionsIntraRouteHandler {

    private final Map<String, WeightObjectivePair<IObjectiveFunctionIntraRoute>> activeObjectiveFunctions;
    private final Map<String, WeightObjectivePair<IObjectiveFunctionIntraRoute>> inactiveObjectiveFunctions;

    public ObjectiveFunctionsIntraRouteHandler() {
        activeObjectiveFunctions = new HashMap<>();
        inactiveObjectiveFunctions = new HashMap<>();
    }

    public ObjectiveFunctionsIntraRouteHandler(ObjectiveFunctionsIntraRouteHandler other) {
        this.activeObjectiveFunctions = new HashMap<>();
        this.activeObjectiveFunctions.putAll(other.activeObjectiveFunctions);
        this.inactiveObjectiveFunctions = new HashMap<>();
        this.inactiveObjectiveFunctions.putAll(other.inactiveObjectiveFunctions);
    }

    /**
     * Objective functions that are active in other is set to active in this and inactive in other are set inactive in this.
     *
     * @param other Other objective to adapt to.
     */
    public void update(ObjectiveFunctionsIntraRouteHandler other) {
        for (String name : other.activeObjectiveFunctions.keySet()) {
            if (this.inactiveObjectiveFunctions.containsKey(name)) {
                WeightObjectivePair<IObjectiveFunctionIntraRoute> objectivePair = this.inactiveObjectiveFunctions.remove(name);
                this.activeObjectiveFunctions.put(name, objectivePair);
            }
        }
        for (String name : other.inactiveObjectiveFunctions.keySet()) {
            if (this.activeObjectiveFunctions.containsKey(name)) {
                WeightObjectivePair<IObjectiveFunctionIntraRoute> objectivePair = this.activeObjectiveFunctions.remove(name);
                this.inactiveObjectiveFunctions.put(name, objectivePair);
            }
        }
    }

    /**
     * Activates an inactive Objective
     *
     * @param name Name to be activated.
     * @return True if variable was activated, otherwise false.
     */
    public boolean activateObjective(String name) {
        WeightObjectivePair<IObjectiveFunctionIntraRoute> constraintToActivate = inactiveObjectiveFunctions.remove(name);
        if (constraintToActivate == null)
            return false;
        activeObjectiveFunctions.put(name, constraintToActivate);
        return true;
    }

    /**
     * Deactivates an active Objective
     *
     * @param name Name to be deactivated.
     * @return True if variable was deactivated, otherwise false.
     */
    public boolean deactivateObjective(String name) {
        WeightObjectivePair<IObjectiveFunctionIntraRoute> constraintToActivate = activeObjectiveFunctions.remove(name);
        if (constraintToActivate == null)
            return false;
        inactiveObjectiveFunctions.put(name, constraintToActivate);
        return true;
    }


    public void addIntraShiftObjectiveFunction(String objectiveFunctionId, double weight, IObjectiveFunctionIntraRoute objectiveIntraShift) {
        activeObjectiveFunctions.put(objectiveFunctionId, new WeightObjectivePair<>(weight, objectiveIntraShift));
    }

    public boolean removeObjective(String name) {
        if (inactiveObjectiveFunctions.remove(name) != null)
            return true;
        return activeObjectiveFunctions.remove(name) != null;

    }

    public WeightObjectivePair<IObjectiveFunctionIntraRoute> getWeightObjectivePair(String name) {
        return activeObjectiveFunctions.getOrDefault(name, inactiveObjectiveFunctions.get(name));

    }

    public void updateObjectiveWeight(String name, double newWeight) {
        WeightObjectivePair<IObjectiveFunctionIntraRoute> obj = activeObjectiveFunctions.getOrDefault(name, inactiveObjectiveFunctions.get(name));
        obj.setWeight(newWeight);
    }

    /**
     * Check if an objective exists, either active or inactive.
     * @param name
     * @return
     */
    public boolean hasObjective(String name) {
        return activeObjectiveFunctions.containsKey(name) || inactiveObjectiveFunctions.containsKey(name);
    }

    public IRouteEvaluatorObjective calculateObjectiveValue(IRouteEvaluatorObjective currentObjective, int travelTime, Visit visit, int startOfServiceNextTask,
                                                            int visitEnd, int syncedVisitLatestStartTime, Shift employeeWorkShift) {

        IRouteEvaluatorObjective newObjective = currentObjective.initializeNewObjective();
        ObjectiveInfo objectiveInfo = new ObjectiveInfo(travelTime, visit, visitEnd, startOfServiceNextTask,
                syncedVisitLatestStartTime, employeeWorkShift);

        for (Map.Entry<String, WeightObjectivePair<IObjectiveFunctionIntraRoute>> objectivePair : activeObjectiveFunctions.entrySet()) {
            newObjective.incrementObjective(objectivePair.getKey(), objectivePair.getValue().getWeight(),
                    objectivePair.getValue().getObjectiveFunction().calculateIncrementalObjectiveValueFor(objectiveInfo));
        }
        return newObjective;
    }


    public Map<String, WeightObjectivePair<IObjectiveFunctionIntraRoute>> getActiveObjectiveFunctions() {
        return activeObjectiveFunctions;
    }

    public Map<String, WeightObjectivePair<IObjectiveFunctionIntraRoute>> getInactiveObjectiveFunctions() {
        return inactiveObjectiveFunctions;
    }
}
