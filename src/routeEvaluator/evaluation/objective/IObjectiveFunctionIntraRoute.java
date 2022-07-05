package routeEvaluator.evaluation.objective;

import routeEvaluator.evaluation.info.ObjectiveInfo;

/**
 * Interface for objectives that is solely evaluated within the route.
 */
public interface IObjectiveFunctionIntraRoute extends IObjectiveFunctionRoute {

    /**
     * The function calculates the objective value of the objective for a part of the route.
     * Here a part of the route is referred to as performing a new task.
     *
     * @param objectiveInfo Holds information regarding the current state of the
     *                                 route used to calculate the objective value.
     * @return The change in objective value for this part of the route.
     */
    double calculateIncrementalObjectiveValueFor(ObjectiveInfo objectiveInfo);



}