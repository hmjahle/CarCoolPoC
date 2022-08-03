package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.OvertimeAbstract;
import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;

public class OvertimeConstraint extends OvertimeAbstract implements IConstraintIntraRoute{
    
    @Override
    public boolean constraintIsFeasible(ConstraintInfo constraintInfo) {
        return !isOverTime(constraintInfo.getEndOfWorkShift(), constraintInfo.getEarliestOfficeReturn());
    }
}
