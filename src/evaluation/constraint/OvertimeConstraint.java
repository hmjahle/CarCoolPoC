package evaluation.constraint;

import evaluation.OvertimeAbstract;
import evaluation.info.ConstraintInfo;

public class OvertimeConstraint extends OvertimeAbstract implements IConstraintIntraRoute{
    
    @Override
    public boolean constraintIsFeasible(ConstraintInfo constraintInfo) {
        return !isOverTime(constraintInfo.getEndOfWorkShift(), constraintInfo.getEarliestOfficeReturn());
    }
}
