package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Shift;
import model.Visit;

public class CarPoolingTimeDependentPairsUtils {

    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    
    /**
     * Assumes:
     *  - That the route (and therefore the shift) is non motorized
     *  - That the join motorized visit and the complete task visit is already added to the shift
     *  - That both of the visits have a time window 
     * @param route The route for the non motorized shift
     * @param joinMotorized The join motorized visit
     * @param dropOff The drop of visit in the transport request
     * @param pickUp The pick-up visit in the transport request
     * @param completeTask The complete task visit that is the successor of the join motorized visit in the route
     * @param syncedVisitsStartTimes start times of already synced visits
     * @param employeeShift The (non motorized) shift that owns the route
     * @return A map of time windows for all relevant visits
     */
    public Map<Visit, List<Integer>> calculateTimeWindowsForNonMotorized(List<Visit> route, Visit joinMotorized, Visit dropOff, Visit pickUp, Visit completeTask, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift){
        Map<Visit, List<Integer>> timeWindows = new HashMap<>();

        // COMPLETE TASK
        List<Integer> completeTaskTimeWindowList = calculateTimeWindow(route, completeTask, syncedVisitsStartTimes, employeeShift);
        timeWindows.put(completeTask, completeTaskTimeWindowList);
        
        // JOIN MOTORIZED
        List<Integer> joinMotorizedTimeWindow = new ArrayList<>();
        int joinMotorizedTimeWindowStart = getTimeWindowStart(route, joinMotorized, syncedVisitsStartTimes, employeeShift);
        int joinMotorizedTimeWindowEnd = completeTaskTimeWindowList.get(END_TIME)-completeTask.getTravelTime();
        joinMotorizedTimeWindow.add(joinMotorizedTimeWindowStart);
        joinMotorizedTimeWindow.add(joinMotorizedTimeWindowEnd);
        timeWindows.put(joinMotorized, joinMotorizedTimeWindow);

        // PICK UP - Same as join motorized
        List<Integer> pickUpTimeWindow= new ArrayList<>();
        pickUpTimeWindow.add(joinMotorizedTimeWindowStart);
        pickUpTimeWindow.add(joinMotorizedTimeWindowEnd);
        timeWindows.put(pickUp, pickUpTimeWindow);
        
        // DROP OFF 
        List<Integer> dropOffTimeWindowList = new ArrayList<>();
        dropOffTimeWindowList.add(joinMotorizedTimeWindowStart + completeTask.getTravelTime());
        dropOffTimeWindowList.add(completeTaskTimeWindowList.get(END_TIME)-completeTask.getVisitDuration());
        timeWindows.put(dropOff, dropOffTimeWindowList);
        
        return timeWindows;
    }

    /**
     * Calculates the proper time windows when a completeTask-visit is inserted between an 
     * existing pick-up and drop-of pair. In a motorized shift  
     * 
     * Assumes:
     *  - Drop-of, Complete Task, and Pick-up is already in the route (in that order)
     *  - Time windows are set for all these visits 
     * @param route The route for the motorized shift 
     * @param joinMotorized The JM-visit that is to be synced with the Pick-up
     * @param dropOff The drop-of visit that corresponds to the completeTask-visit
     * @param pickUp The pick-up visit that corresponds to the completeTask-visit
     * @param completeTask the complete task that we inserted
     * @param syncedVisitsStartTimes The current syncronized start times
     * @param employeeShift Shift that own the route 
     * @return List of time windows for the nodes
     */
    public Map<Visit, List<Integer>> calculateTimeWindowsForMotorized(List<Visit> route, Visit joinMotorized, Visit dropOff, Visit pickUp, Visit completeTask, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift) {
        Map<Visit, List<Integer>> timeWindows = new HashMap<>();

        // PICK UP
        List<Integer> pickUpTimeWindow = calculateTimeWindow(route, pickUp, syncedVisitsStartTimes, employeeShift);
        timeWindows.put(pickUp, pickUpTimeWindow); 
            
        // JOIN MOTORIZED - Have the same as the pick up visit
        List<Integer> joinMotorizedTimeWindow = new ArrayList<>();
        joinMotorizedTimeWindow.add(pickUpTimeWindow.get(START_TIME));
        joinMotorizedTimeWindow.add(pickUpTimeWindow.get(END_TIME));
        timeWindows.put(joinMotorized, joinMotorizedTimeWindow);

        // COMPLETE TASK - end time depend on pick up, start time must be calculated
        List<Integer> completeTaskTimeWindow= new ArrayList<>();

        // ToDo: you have already calculated this when you run the  'calculateTimeWindow' method on the pickUp visit.
        //  this means that you can speed up the calculations with some dynamic programming
        int completeTaskTimeWindowStart = getTimeWindowStart(route, completeTask, syncedVisitsStartTimes, employeeShift);
        int completeTaskTimeWindowEnd = pickUpTimeWindow.get(END_TIME) - completeTask.getVisitDuration();
        completeTaskTimeWindow.add(completeTaskTimeWindowStart); 
        completeTaskTimeWindow.add(completeTaskTimeWindowEnd); 
        timeWindows.put(completeTask, completeTaskTimeWindow);
            
        // DROP OFF - Same as the complete task visit
        List<Integer> dropOffTimeWindow = new ArrayList<>();
        dropOffTimeWindow.add(completeTaskTimeWindowStart);
        dropOffTimeWindow.add(completeTaskTimeWindowEnd);
        timeWindows.put(dropOff, dropOffTimeWindow);
        
        return timeWindows;
    }


    /**
     * Calculates the time window start (i.e., the earliest possible start time) for the visit. Calculation is done so that the route is feasible 
     * with respect to time windows
     * @param route
     * @param currentVisit
     * @param syncedVisitsStartTime
     * @return time window start for the visit. Null if visit is not in the route 
     * @throws NullPointerException if the start time of any visit of the route is not set
     */
    public Integer getTimeWindowStart(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift) throws NullPointerException{
        int startTime;
        int previousVisitEndTime = employeeShift.getStartTime();
        for(Visit visit : route){
            if (visit.getTimeWindowStart() == null){
                throw new NullPointerException("Time window start of visit is not initialized");
            }
            if (syncedVisitsStartTimes.containsKey(visit)){
                // If the synced visit is Synced With Interval Diff then it can start after the synced visit start, but never before.
                // The visit can never start before the visit start interval. 
                // NB!! is visit.getTravelTime updated?
                startTime = Math.max(Math.max(previousVisitEndTime + visit.getTravelTime(), visit.getTimeWindowStart()), syncedVisitsStartTimes.get(visit));
            } else {
                startTime = Math.max(previousVisitEndTime + visit.getTravelTime(), visit.getTimeWindowStart());
            }
            previousVisitEndTime = startTime + visit.getVisitDuration();
            if (visit == currentVisit){
                return startTime;
            }
        }
        return null;
    }

    /**
     * Calculates the time window end (i.e., the latest possible start time) for the visit. Calculation is done so that the route is feasible 
     * with respect to time windows
     * @param route
     * @param currentVisit
     * @param syncedVisitsStartTimes
     * @param employeeShift
     * @return The time window end. 
     * @throws NullPointerException if the visit is not in the route
     */
    private Integer getTimeWindowEnd(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift){
        int latestStartTime = Math.min(route.get(-1).getTaskEndTime(), employeeShift.getEndTime());
        int previousVisitTravelTime = 0;
        for (int i=route.size(); i-- > 0;){
            Visit visit = route.get(i);
            if (visit.getTimeWindowEnd() == null){
                throw new NullPointerException("End time of visit is not initialized");
            }
            if (syncedVisitsStartTimes.containsKey(visit)){
                latestStartTime = syncedVisitsStartTimes.get(visit)+visit.getTimeDependentOffsetInterval();
            } else {
                latestStartTime = Math.max(Math.min(latestStartTime - previousVisitTravelTime - visit.getVisitDuration(),  visit.getTimeWindowEnd()-visit.getVisitDuration()), visit.getTimeWindowStart());
            }
            previousVisitTravelTime = visit.getTravelTime();
            if (visit == currentVisit){
                return latestStartTime + visit.getVisitDuration();
            }
        }
        return null;

    }

    /**
     * Calculate the time window for a visit. A time window is an interval of the earliest possible start time
     * and the latest possible start time for the given visit in the given route.
     * @param route The route we use to cacluate the time window 
     * @param currentVisit The visit we wish to find the time window for
     * @param syncedVisitsStartTimes Start times of synced visits we have to take into account when calculating the time window
     * @param employeeShift The shift that owns the route
     * @return A list of integers on the format [earliestPossibleStartTime, latestPossibleStartTime]
     */
    public List<Integer> calculateTimeWindow(List<Visit> route, Visit currentVisit, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift) {
        List<Integer> timeWindowStartTime = new ArrayList<>();
        timeWindowStartTime.add(getTimeWindowStart(route, currentVisit, syncedVisitsStartTimes, employeeShift));
        timeWindowStartTime.add(getTimeWindowEnd(route, currentVisit, syncedVisitsStartTimes, employeeShift));
        return timeWindowStartTime;
    }
}