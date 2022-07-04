package evaluation.constraint;

import evaluation.CustomCriteriaAbstract;
import evaluation.info.ConstraintInfo;
import evaluation.info.RouteEvaluationInfoAbstract;
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
