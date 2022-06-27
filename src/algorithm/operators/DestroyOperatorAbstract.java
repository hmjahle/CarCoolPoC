package algorithm.operators;

import java.util.Random;

/**
 *
 */
public class DestroyOperatorAbstract extends OperatorAbstract {


    private static final int MAX_NUMBER_OF_RANDOM_TASKS_TO_REMOVE = 30;
    private static final int MIN_NUMBER_OF_RANDOM_TASKS_TO_REMOVE = 10;

    protected DestroyOperatorAbstract() {
        this(new Random());
    }

    protected DestroyOperatorAbstract(Random random) {
        super(random);
    }

    /**
     * Finds the number of tasks to remove, finds a minimum of 1 task and a maximum given by a fixed constant.
     * However, it can never remove more tasks than there are present in the solution.
     *
     * @param allocatedTasksInTheSolution Total tasks in the current solution.
     * @return Number of tasks to remove.
     */
    public int findNumberOfTasksToRemove(int allocatedTasksInTheSolution) {
        int removeMin = Math.min(MIN_NUMBER_OF_RANDOM_TASKS_TO_REMOVE, (int) (allocatedTasksInTheSolution * .1));
        removeMin = Math.max(removeMin, 1);
        int removeMax = Math.min(MAX_NUMBER_OF_RANDOM_TASKS_TO_REMOVE, (int) (allocatedTasksInTheSolution * .4));
        removeMax = Math.max(removeMax, 1);
        if (removeMin == removeMax)
            return removeMin;
        return random.nextInt((removeMax - removeMin) + 1) + removeMin;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}