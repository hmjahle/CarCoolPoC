package evaluation.routeEvaluator.results;

import model.Task;
import model.Visit;
import util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Route {

    private final List<Visit> visitSolution;
    private int routeFinishedAtTime;

    public Route() {
        this.visitSolution = new ArrayList<>();
        this.routeFinishedAtTime = 0;
    }

    public void addVisits(List<Visit> visits) {
        visitSolution.addAll(visits);
    }

    public void setRouteFinishedAtTime(int routeFinishedAtTime) {
        this.routeFinishedAtTime = routeFinishedAtTime;
    }

    public int getRouteFinishedAtTime() {
        return routeFinishedAtTime;
    }

    public List<Visit> getVisitSolution() {
        return visitSolution;
    }

    /**
     * Extracts the visits from the route, they are returned in the correct order.
     *
     * @return List of visits, can be empty.
     */
    public List<Visit> extractEmployeeVisits() {
        return visitSolution;
    }

    /**
     * Extracts the tasks from the route, they are returned in the correct order.
     *
     * @return List of visits, can be empty.
     */
    public List<Task> extractEmployeTasks() {
        List<Task> employeTasks = new ArrayList<Task>();
        for (int i=0; i<visitSolution.size()-1; i++){
            if (visitSolution.get(i+1).getTransportType() == Constants.TransportMode.WALK){
                employeTasks.add(visitSolution.get(i).getTask());
            }
        }
        return employeTasks;
    }

    /**
     * Extracts the visits, where the task is synced, from the route,
     * they are returned as a set and hence no order is guaranteed.
     *
     * @return The set of visits, can be empty.
     */
    /* public Set<Visit<T>> extractSyncedVisits() {
        return this.getVisitSolution().stream().filter(i -> i.getTask().isSynced()).collect(Collectors.toSet());
    } */

    /**
     * Extracts the visits, where the task is strict, from the route,
     * they are returned as a set and hence no order is guaranteed.
     *
     * @return The set of visits, can be empty.
     */
    public Set<Visit> extractStrictVisits() {
        return this.getVisitSolution().stream().filter(i -> i.getTask().isStrict()).collect(Collectors.toSet());
    }

    /**
     * Finds the position of the task in the route.
     *
     * @param task Task to find the position for.
     * @return Integer position, null if the task is not in the route.
     */
    public Integer findIndexInRouteTask(Task task) {
        int i = 0;
        for (Visit visit : visitSolution)
            if (visit.getTask() == task)
                return i;
            else i++;
        return null;
    }

    /**
     * Finds the position of the visit in the route.
     *
     * @param visitToBeFound Visit to find the position for.
     * @return Integer position, null if the task is not in the route.
     */
    public Integer findIndexInRouteVisit(Visit visitToBeFound) {
        int i = 0;
        for (Visit visit : visitSolution)
            if (visit == visitToBeFound)
                return i;
            else i++;
        return null;
    }
}
