package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;

public interface IConstraintIntraRoute {

    boolean constraintIsFeasible(ConstraintInfo constraintInfo);
}
