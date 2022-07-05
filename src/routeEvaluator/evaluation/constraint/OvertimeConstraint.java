package routeEvaluator.evaluation.constraint;

import routeEvaluator.evaluation.OvertimeAbstract;
import routeEvaluator.evaluation.info.ConstraintInfo;

public class OvertimeConstraint extends OvertimeAbstract implements IConstraintIntraRoute{
    
    @Override
    public boolean constraintIsFeasible(ConstraintInfo constraintInfo) {
        return !isOverTime(constraintInfo.getEndOfWorkShift(), constraintInfo.getEarliestOfficeReturn());
    }
}
