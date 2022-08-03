package util;

import model.TimeDependentVisitPair;
import solution.Solution;

public class SynchronizedTaskUtils {
    

    private SynchronizedTaskUtils() {
        // don't use constructor
    }

    public static boolean isStartTimeInvalid(Solution solution, TimeDependentVisitPair pair) {
        if (!solution.isVisitAllocated(pair.getMasterVisit()) || !solution.isVisitAllocated(pair.getDependentVisit())) {
            return false;
        }

        int masterStartTime = solution.getSyncedVisitStartTimes().get(pair.getMasterVisit());
        int dependentStartTime = solution.getSyncedVisitStartTimes().get(pair.getDependentVisit());

        return isStartTimeInvalid(masterStartTime, dependentStartTime, pair.getIntervalStart(), pair.getIntervalEnd());
    }

    public static boolean isStartTimeInvalid(int masterStartTime, int dependentStartTime, int intervalStart, int intervalEnd) {
        int validDependentIntervalStart = masterStartTime + intervalStart;
        int validDependentIntervalEnd = masterStartTime + intervalEnd;

        return validDependentIntervalStart - Constants.SYNCED_TASK_CONSTRAINT_ALLOWED_SLACK_DEFAULT >= dependentStartTime
                || validDependentIntervalEnd + Constants.SYNCED_TASK_CONSTRAINT_ALLOWED_SLACK_DEFAULT <= dependentStartTime;
    }


    public static boolean isSyncedTimeWindowsInvalid(int masterStartWindowStart, int masterStartWindowEnd,
                                                     int dependentStartWindowStart, int dependentStartWindowEnd,
                                                     int minOffset, int maxOffset) {

        return !MathUtils.doInclusiveIntervalsOverlap(
                masterStartWindowStart + minOffset,
                masterStartWindowEnd + maxOffset,
                dependentStartWindowStart,
                dependentStartWindowEnd
        );
    }

    public static boolean isNotSameAllocationState(Solution solution, TimeDependentVisitPair pair) {
        return solution.isVisitAllocated(pair.getMasterVisit()) != solution.isVisitAllocated(pair.getDependentVisit());
    }
}
