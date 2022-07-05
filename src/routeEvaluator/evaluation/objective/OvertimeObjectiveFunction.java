package routeEvaluator.evaluation.objective;

import routeEvaluator.evaluation.OvertimeAbstract;
import routeEvaluator.evaluation.info.ObjectiveInfo;

public class OvertimeObjectiveFunction extends OvertimeAbstract implements IObjectiveFunctionIntraRoute {

    @Override
    public double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo) {
        if (objectiveInfo.isDepot()) {
            long workShiftEnd = objectiveInfo.getEndOfWorkShift();
            long officeReturn = objectiveInfo.getStartOfServiceNextTask();
            if (isOverTime(workShiftEnd, officeReturn)) {
                return (double) (officeReturn - workShiftEnd);
            }
        }
        return 0.0;
    }

}