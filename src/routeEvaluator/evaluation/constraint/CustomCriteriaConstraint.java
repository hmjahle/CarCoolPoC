package routeEvaluator.evaluation.constraint;

import java.util.function.Function;

import routeEvaluator.evaluation.CustomCriteriaAbstract;
import routeEvaluator.evaluation.info.ConstraintInfo;
import routeEvaluator.evaluation.info.RouteEvaluationInfoAbstract;

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
