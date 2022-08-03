package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class ConstraintsIntraRouteHandler {

    private final Map<String, IConstraintIntraRoute> activeConstraints;
    private final Map<String, IConstraintIntraRoute> inactiveConstraints;
    private boolean checkAllConstraints;

    public ConstraintsIntraRouteHandler() {
        activeConstraints = new LinkedHashMap<>();
        inactiveConstraints = new LinkedHashMap<>();
    }

    public ConstraintsIntraRouteHandler(ConstraintsIntraRouteHandler other) {
        this.checkAllConstraints = other.checkAllConstraints;
        this.activeConstraints = new LinkedHashMap<>();
        this.activeConstraints.putAll(other.activeConstraints);
        this.inactiveConstraints = new LinkedHashMap<>();
        this.inactiveConstraints.putAll(other.inactiveConstraints);
    }

    public void update(ConstraintsIntraRouteHandler other) {
        this.checkAllConstraints = other.checkAllConstraints;
        for (String name : other.activeConstraints.keySet()) {
            if (this.inactiveConstraints.containsKey(name)) {
                IConstraintIntraRoute cons = this.inactiveConstraints.remove(name);
                this.activeConstraints.put(name, cons);
            }
        }
        for (String name : other.inactiveConstraints.keySet()) {
            if (this.activeConstraints.containsKey(name)) {
                IConstraintIntraRoute cons = this.activeConstraints.remove(name);
                this.inactiveConstraints.put(name, cons);
            }
        }
    }

    public void activateCheckAllActiveAndInactiveConstraints() {
        checkAllConstraints = true;
    }

    public void deActivateCheckAllActiveAndInactiveConstraints() {
        checkAllConstraints = false;
    }

    public boolean isFeasible(ConstraintInfo constraintInfo) {
        for (IConstraintIntraRoute constraint : activeConstraints.values()) {
            if (!constraint.constraintIsFeasible(constraintInfo)) {
                return false;
            }
        }
        if (checkAllConstraints) {
            return relaxedConstraintsAreFeasible(constraintInfo);
        }
        return true;
    }

    private boolean relaxedConstraintsAreFeasible(ConstraintInfo constraintInfo) {
        for (IConstraintIntraRoute constraint : inactiveConstraints.values()) {
            if (!constraint.constraintIsFeasible(constraintInfo)) {
                return false;
            }
        }
        return true;
    }

    public void addConstraint(String name, IConstraintIntraRoute constraint) {
        activeConstraints.put(name, constraint);
    }

    public void addConstraint(IConstraintIntraRoute constraint) {
        activeConstraints.put(constraint.getClass().getSimpleName(), constraint);
    }

    /**
     * Activates an inactive constraint
     *
     * @param name Constraint name to be activated.
     * @return True if variable was activated, otherwise false.
     */
    public boolean activateConstraint(String name) {
        IConstraintIntraRoute constraintToActivate = inactiveConstraints.remove(name);
        if (constraintToActivate == null)
            return false;
        activeConstraints.put(name, constraintToActivate);
        return true;
    }

    /**
     * Deactivates an active constraint
     *
     * @param name Constraint name to be deactivated.
     * @return True if variable was deactivated, otherwise false.
     */
    public boolean deactivateConstraint(String name) {
        IConstraintIntraRoute constraintToActivate = activeConstraints.remove(name);
        if (constraintToActivate == null)
            return false;
        inactiveConstraints.put(name, constraintToActivate);
        return true;
    }

    public Map<String, IConstraintIntraRoute> getActiveConstraintsMap() {
        return activeConstraints;
    }

    public Map<String, IConstraintIntraRoute> getInactiveConstraintsMap() {
        return inactiveConstraints;
    }

    public Collection<IConstraintIntraRoute> getActiveConstraints() {
        return activeConstraints.values();
    }

    public Collection<IConstraintIntraRoute> getInactiveConstraints() {
        return inactiveConstraints.values();
    }
}
