package algorithm.operators;

import model.Model;
import model.Shift;
import model.Task;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Mixin like class for operators.
 */
public abstract class OperatorAbstract {

    private static final double MAX_NOISE_ON_OBJECTIVE_VALUE = .05;

    protected Random random;

    protected OperatorAbstract(Random random) {
        this.random = random;
    }


    public boolean canBeMerged(Model model, Shift shift, List<Task> currentTasks, List<Task> mergeTasks) {
        // Assumes all tasks are compatible
        return true;
    }


    public static boolean adheresToModelConventions(Model model, List<Task> route, Shift shift) {
        return isCompatibleWithTask(model, route, shift) &&
                !containsIncompatibleTasks(model, route);
    }

    private static boolean isCompatibleWithTask(Model model, List<Task> route, Shift shift) {
        // Assume all tasks and shifts are compatible
        return true;
    }

    private static boolean containsIncompatibleTasks(Model model, List<Task> route) {
        // Assume no tasks are incompatible
        return false;
    }

    public static double noise(Random random) {
        return (1 - MAX_NOISE_ON_OBJECTIVE_VALUE / 2) + MAX_NOISE_ON_OBJECTIVE_VALUE * random.nextDouble();
    }


}
