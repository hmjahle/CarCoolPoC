package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Shift;
import model.TimeDependentVisitPair;
import model.Visit;

public class CarPoolingTimeDependentPairsUtils {

    static int START_TIME = 0;
    static int END_TIME=1;

    public Map<Visit, List<Integer>> calculateTimeWindows(List<Visit> route, Visit joinMotorized, Visit dropOff, Visit pickUp, Visit completeTask, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift){
        // NB!!!! Assume that the route have already contains the task joinMotorized and complete task in its route and that join motorized has a getStartTime
        Map<Visit, List<Integer>> timeWindows = new HashMap<>();
        List<Integer> completeTaskTimeWindowList = calculateTimeWindow(route, completeTask, syncedVisitsStartTimes, employeeShift);
        
        List<Integer> pickUpTimeWindowList = new ArrayList<>();
        int pickUpStartTime = getVisitStartTime(route, joinMotorized, syncedVisitsStartTimes, employeeShift);
        pickUpTimeWindowList.add(pickUpStartTime);
        pickUpTimeWindowList.add(completeTaskTimeWindowList.get(END_TIME)-completeTask.getTravelTime());

        List<Integer> dropOffTimeWindowList = new ArrayList<>();
        dropOffTimeWindowList.add(pickUpStartTime+completeTask.getTravelTime());
        dropOffTimeWindowList.add(completeTaskTimeWindowList.get(END_TIME)-completeTask.getVisitDuration());

        timeWindows.put(completeTask, completeTaskTimeWindowList);
        timeWindows.put(pickUp, pickUpTimeWindowList);
        timeWindows.put(dropOff, dropOffTimeWindowList);

        return timeWindows;
    }


    /**
     * Calculates start time for a visit in a route
     * @param route
     * @param currentVisit
     * @param syncedVisitsStartTime
     * @return the earliest possible start time of the visit in the route. Null if visit is not in the route. 
     */
    public Integer getVisitStartTime(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift) throws NullPointerException{
        int startTime;
        int previousVisitEndTime = employeeShift.getStartTime();
        for(Visit visit : route){
            if (visit.getStartTime() == null){
                throw new NullPointerException("Start time of visit is not initialized");
            }
            if (syncedVisitsStartTimes.containsKey(visit)){
                // If the synced visit is Synced With Interval Diff then it can start after the synced visit start, but never before.
                // The visit can never start before the visit start interval. 
                // NB!! is visit.getTravelTime updated?
                startTime = Math.max(Math.max(previousVisitEndTime + visit.getTravelTime(), visit.getStartTime()), syncedVisitsStartTimes.get(visit));
            } else {
                startTime = Math.max(previousVisitEndTime + visit.getTravelTime(), visit.getStartTime());
            }
            previousVisitEndTime = startTime + visit.getVisitDuration();
            if (visit == currentVisit){
                return startTime;
            }
        }
        return null;
    }

    private Integer getVisitEndTime(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift){
        int latestStartTime = Math.min(route.get(-1).getTaskEndTime(), employeeShift.getEndTime());
        int previousVisitTravelTime = 0;
        for (int i=route.size(); i-- > 0;){
            Visit visit = route.get(i);
            if (visit.getEndTime() == null){
                throw new NullPointerException("End time of visit is not initialized");
            }
            if (syncedVisitsStartTimes.containsKey(visit)){
                latestStartTime = syncedVisitsStartTimes.get(visit)+visit.getTimeDependentOffsetInterval();
            } else {
                latestStartTime = Math.max(Math.min(latestStartTime - previousVisitTravelTime - visit.getVisitDuration(),  visit.getEndTime()-visit.getVisitDuration()), visit.getStartTime());
            }
            previousVisitTravelTime = visit.getTravelTime();
            if (visit == currentVisit){
                return latestStartTime + visit.getVisitDuration();
            }
        }
        return null;

    }

    public List<Integer> calculateTimeWindow(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift) {
        List<Integer> timeWindowStartTime = new ArrayList<>();
        timeWindowStartTime.add(getVisitStartTime(route, currentVisit, syncedVisitsStartTimes, employeeShift));
        timeWindowStartTime.add(getVisitEndTime(route, currentVisit, syncedVisitsStartTimes, employeeShift));
        return timeWindowStartTime;
    }

    public TimeDependentVisitPair createCarpoolTimeDependentPair(Visit masterVisit, int masterShiftId, Visit dependentVisit, int dependentShiftId, int syncedStartTime, int intervalOffset, Map<Visit, Integer> carpoolSyncedVisitStartTime){
        masterVisit.setCarpooling(dependentShiftId, syncedStartTime, 0);
        dependentVisit.setCarpooling(masterShiftId, syncedStartTime, intervalOffset);
        carpoolSyncedVisitStartTime.put(masterVisit, syncedStartTime);
        carpoolSyncedVisitStartTime.put(dependentVisit, syncedStartTime);
        return new TimeDependentVisitPair(masterVisit, dependentVisit, 0, intervalOffset);
    }
}