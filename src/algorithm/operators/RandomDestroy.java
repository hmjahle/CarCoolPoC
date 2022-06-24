package com.visma.of.orp.solver.algorithm.operators;

import com.visma.of.orp.solver.algorithm.NeighborhoodMoveInfo;
import com.visma.of.common.model.Model;
import com.visma.of.common.model.Shift;
import com.visma.of.common.model.Task;
import com.visma.of.common.solution.Problem;
import com.visma.of.common.solution.Solution;

import java.util.*;

/**
 * The Random destroy operator removes the a random number of uniformly selected tasks from the solution.
 */
public class RandomDestroy extends DestroyOperatorAbstract implements IDestroyOperator {

    private Model model;
    private int[] totalPositions;

    public RandomDestroy(Model model) {
        super();
        this.model = model;
        totalPositions = new int[model.getTasks().size()];
    }

    /**
     * Removes a number of random tasks uniformly from the solution.
     * If it is not possible to remove a task from the solution, because it is empty or will render it infeasible
     * it will return null.
     *
     * @param problem Original problem (with solution, objectives and constraints) to remove the tasks from, must be
     *                build from the same model used to create the operator. (the input solution will be changed).
     * @return NeighborhoodMoveInfo or null.
     */
    @Override
    public NeighborhoodMoveInfo destroy(Problem problem) {
        Solution solution = problem.getSolution();
        int allocatedTasks = solution.totalAllocatedTasks();
        if (allocatedTasks == 0)
            return null;

        int removeNumberOfTasks = findNumberOfTasksToRemove(allocatedTasks);
        List<Integer> positions = findPositionsWhereToRemoveTasks(allocatedTasks, removeNumberOfTasks);
        Map<Shift, List<Integer>> removedTasks = findTaskIndicesToRemove(solution, positions);

        Double deltaObjectiveValue = updateNewSolutionAndObjective(problem, removedTasks);
        if (deltaObjectiveValue == null)
            return null;
        return new NeighborhoodMoveInfo(problem, deltaObjectiveValue);
    }

    /**
     * An ordered list of positions (indices) in the solution where to remove tasks. Here the indices refers to a position
     * in a route for a shift. E.g., index 0, is task 0 in shift 0, index 3 can be task 1 in shift 1 if shift 0 has only 1 task.
     * E.g. a solution with 10 tasks and 3 shift could look like this {[0],[0,1,2],[0,1,2,3,4,5]} ==> {[0],[1,2,3],[4,5,6,7,8,9]}.
     * Here removing 3 tasks could return [1,5,8].
     *
     * @param numberOfAllocatedTasks Total number of tasks allocated in the solution (actually allocated to the shifts).
     * @param removeNumberOfTasks    Number of tasks to remove.
     * @return List of positions from where tasks should be removed.
     */
    private List<Integer> findPositionsWhereToRemoveTasks(int numberOfAllocatedTasks, int removeNumberOfTasks) {
        List<Integer> positions = new ArrayList<>();
        resetArray(numberOfAllocatedTasks);
        int randomNumber;
        for (int i = 0; i < removeNumberOfTasks; i++) {
            randomNumber = random.nextInt(numberOfAllocatedTasks - i);
            int position = totalPositions[randomNumber];
            totalPositions[randomNumber] = totalPositions[numberOfAllocatedTasks - i - 1];
            positions.add(position);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Updates the solution and objectives by removing the tasks from all the affected shifts.
     * During the update it maintains the delta objective values and returns it at the end.
     *
     * @param removedTasks Shifts with task indices to be removed from the solution
     * @param problem      Problem to be updated
     * @return Total delta objective value or null if infeasible.
     */
    private Double updateNewSolutionAndObjective(Problem problem, Map<Shift, List<Integer>> removedTasks) {
        double deltaObjectiveValue = 0;
        Solution solution = problem.getSolution();
        for (Map.Entry<Shift, List<Integer>> kvp : removedTasks.entrySet()) {
            Shift shift = kvp.getKey();
            List<Integer> removalPositions = kvp.getValue();
            Double intraObj = problem.getObjective()
                    .deltaIntraObjectiveNewRoute(shift, solution
                            .getRoute(shift), removalPositions, solution.getSyncedTaskStartTimes());
            if (intraObj == null)
                return null;
            List<Task> route = solution.getRoute(shift);
            if (!problem.getConstraints().isFeasibleRemoveTasks(shift, route, removalPositions))
                return null;
            double extraObj = problem.getObjective().deltaExtraRouteObjectiveValueRemove(shift, route, removalPositions);
            deltaObjectiveValue += intraObj + extraObj;
            problem.unAssignTasksByRouteIndices(shift, route, removalPositions, intraObj, extraObj);
        }
        return deltaObjectiveValue;
    }


    /**
     * Using a list of positions in a solutions shifts from which tasks are removed, the task indices inside the shifts
     * are found and returned. This is done for all affected shifts.
     *
     * @param solution  The solution to remove tasks from.
     * @param positions List of position in the @param solution where tasks should be removed. The list must be ordered
     *                  ascending and all positions must be unique and inside the solution.
     * @return Map of the shifts that are affected and the indices for tasks to be removed inside each shift/route in the solution.
     */
    private Map<Shift, List<Integer>> findTaskIndicesToRemove(Solution solution, List<Integer> positions) {
        Map<Shift, List<Integer>> removedTasks = new HashMap<>();
        int tasksInspected = 0;
        int positionNo = 0;
        int position = positions.get(0);
        Iterator<Shift> shiftIterator = model.getShifts().iterator();
        Shift shift = shiftIterator.next();
        boolean searchNextPosition = true;
        while (searchNextPosition) {
            if (position < tasksInspected + solution.getNumberOfTasksInShift(shift)) {
                if (addRemovedTask(positions, removedTasks, tasksInspected, positionNo, position, shift, solution)) {
                    searchNextPosition = false;
                    continue;
                }
                positionNo++;
                position = positions.get(positionNo);
            } else if (shiftIterator.hasNext()) {
                tasksInspected += solution.getNumberOfTasksInShift(shift);
                shift = shiftIterator.next();
            } else
                searchNextPosition = false;
        }
        return removedTasks;
    }

    /**
     * Adds position of the task to remove inside the route of the shift. It also checks if there are more positions
     * to check and returns true if there are more positions or false if there are no more positions to check.
     *
     * @param positions
     * @param removedTasks
     * @param tasksInspected
     * @param positionNo
     * @param position
     * @param shift
     * @return True if there are more positions to check, otherwise false.
     */
    private boolean addRemovedTask(List<Integer> positions, Map<Shift, List<Integer>> removedTasks, int tasksInspected,
                                   int positionNo, int position, Shift shift, Solution solution) {
        removedTasks.putIfAbsent(shift, new ArrayList<>());
        if (!solution.getRoute(shift).get(position - tasksInspected).isPrioritized()) {
            removedTasks.get(shift).add(position - tasksInspected);
        }
        return positionNo == positions.size() - 1;
    }

    private void resetArray(int allocatedTasks) {
        for (int i = 0; i < allocatedTasks; i++)
            totalPositions[i] = i;
    }

}