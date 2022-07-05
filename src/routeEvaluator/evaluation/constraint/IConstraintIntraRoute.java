package routeEvaluator.evaluation.constraint;

import routeEvaluator.evaluation.info.ConstraintInfo;

public interface IConstraintIntraRoute {

    boolean constraintIsFeasible(ConstraintInfo constraintInfo);
}
