package routeEvaluator.evaluation.objective;

import java.util.Collection;
import java.util.List;

import algorithm.Solution;
import model.Shift;
import model.Visit;

public interface IObjectiveFunction extends IObjectiveFunctionRoute {

    /**
     * Calculate a double value representing the objective. This is based on the entire route of the employee.
     *
     * @param shift Shift that is considered.
     * @param Visits List of Visits that are in the current route.
     * @return Double.
     */
    double calculateObjectiveValueFor(Shift shift, List<Visit> visits);

    /**
     * Calculate a double value representing the change in objective value when removing a single Visit from the current route.
     * This is based on the entire route of the employee.
     *
     * @param shift       Shift that is considered.
     * @param removedVisit Visits that are being removed from the route.
     * @return Change in the objective value when removing the Visits from the route.
     */
    double calculateDeltaObjectiveValueRemovingVisit(Shift shift, Visit removedVisit);

    /**
     * Calculate a double value representing the change in objective value when removing Visits from the current route.
     * This is based on the entire route of the employee.
     *
     * @param shift        Shift that is considered.
     * @param currentRoute List of Visits that are in the current route, the Visits that are removed from the route must be in this list.
     * @param removedVisits Collection of Visits by index given as indices(sorted) in the list of @param currentRoute.
     * @return Change in the objective value when removing the Visits from the route.
     */
    double calculateDeltaObjectiveValueRemovingVisits(Shift shift, List<Visit> currentRoute, Collection<Integer> removedVisits);

    IObjectiveFunction copy();

    /**
     * Calculate a double value representing the change in objective value when removing Visits from the current route.
     * This is based on the entire route of the employee.
     *
     * @param shift        Shift that is considered.
     * @param removedVisits List of Visits that are being removed from the route.
     * @return Change in the objective value when removing the Visits from the route.
     */
    double calculateDeltaObjectiveValueRemovingVisits(Shift shift, List<Visit> removedVisits);


    /**
     * Calculate a double value representing the change in objective value when adding Visits to the current route.
     * This is based on the entire route of the employee.
     *
     * @param shift      Shift that is considered.
     * @param addedVisits List of Visits that are being added to the route.
     * @return Change in the objective value when removing the Visits from the route.
     */
    double calculateDeltaObjectiveValueAddingVisits(Shift shift, List<Visit> addedVisits);

    /**
     * Calculate a double value representing the change in objective value when adding Visits to the current route.
     * This is based on the entire route of the employee.
     *
     * @param shift     Shift that is considered.
     * @param addedVisit List of Visits that are being added to the route.
     * @return Change in the objective value when removing the Visits from the route.
     */
    double calculateDeltaObjectiveValueAddingVisit(Shift shift, Visit addedVisit);

    /**
     * When adding Visits affect the state of the objective that is necessary to calculate the delta objective values
     * this must be implemented with updating whichever data necessary.
     *
     * @param shift      Shift Visits are being added to.
     * @param addedVisits Visits being added.
     */
    void addingVisits(Shift shift, Collection<Visit> addedVisits);

    /**
     * When adding Visits that affect the state of the objective it is necessary to calculate the delta objective values
     * this must be implemented with updating whichever data necessary.
     *
     * @param shift   Shift Visit is being added to.
     * @param addVisit Visit being added.
     */
    void addingVisit(Shift shift, Visit addVisit);

    /**
     * When removing Visits affect the state of the objective that is necessary to calculate the delta objective values
     * this must be implemented with updating whichever data necessary.
     *
     * @param shift        Shift Visits are being removed from.
     * @param removedVisits Visits being removed.
     */
    void removingVisits(Shift shift, Collection<Visit> removedVisits);

    /**
     * When removing Visits affect the state of the objective that is necessary to calculate the delta objective values
     * this must be implemented with updating whichever data necessary.
     *
     * @param shift       Shift Visits are being removed from.
     * @param removedVisit Visit being removed.
     */
    void removingVisit(Shift shift, Visit removedVisit);

    /**
     * If the objective need a state to calculate the delta objective values this must be implemented. Here all necessary
     * data from the other objective function should be copied such that this objective functions state resembles the
     * other.
     *
     * @param other Objective function to update from.
     */
    void update(IObjectiveFunction other);

    void updateState(Solution solution);

}
