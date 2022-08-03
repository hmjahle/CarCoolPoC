package com.visma.of.cps.routeEvaluator.evaluation.constraint;

import com.visma.of.cps.routeEvaluator.evaluation.CustomCriteriaAbstract;
import com.visma.of.cps.routeEvaluator.evaluation.info.ConstraintInfo;
import com.visma.of.cps.routeEvaluator.evaluation.info.RouteEvaluationInfoAbstract;

import java.util.function.Function;

public class CustomCriteriaConstraint extends CustomCriteriaAbstract implements IConstraintIntraRoute{

    private final Function<ConstraintInfo, Boolean> constraint;

    public CustomCriteriaConstraint(Function<RouteEvaluationInfoAbstract, Boolean> criteriaFunction,
                                    Function<ConstraintInfo, Boolean> constraint) {
        super(criteriaFunction);
        this.constraint = constraint;
    }

    @Override
    public boolean constraintIsFeasible(ConstraintInfo constraintInfo) {
        if (!criteriaIsFulfilled(constraintInfo))
            return true;
        return constraint.apply(constraintInfo);
    }
    
}
