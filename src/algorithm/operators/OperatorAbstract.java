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
        for (Task task : mergeTasks) {
            if (!model.getCompatibleShifts().get(task).contains(shift))
                return false;
        }
        for (Task currentTask : currentTasks) {
            Set<Task> incompatibleTasks = model
                    .getIncompatibleTasks()
                    .get(currentTask);
            if (incompatibleTasks == null || incompatibleTasks.isEmpty())
                continue;
            for (Task mergeTask : mergeTasks)
                if (incompatibleTasks.contains(mergeTask)) {
                    return false;
                }
        }
        return true;
    }


    public static boolean adheresToModelConventions(Model model, List<Task> route, Shift shift) {
        return isCompatibleWithTask(model, route, shift) &&
                !containsIncompatibleTasks(model, route);
    }

    private static boolean isCompatibleWithTask(Model model, List<Task> route, Shift shift) {
        for (Task task : route) {
            if (!model.getCompatibleShifts()
                    .get(task)
                    .contains(shift))
                return false;
        }
        return true;
    }

    private static boolean containsIncompatibleTasks(Model model, List<Task> route) {
        for (int i = 0; i < route.size() - 1; i++) {
            Set<Task> incompatibleTasks = model
                    .getIncompatibleTasks()
                    .get(route.get(i));
            for (int j = i + 1; j < route.size(); j++) {
                if (incompatibleTasks != null && incompatibleTasks.contains(route.get(j)))
                    return true;
            }
        }
        return false;
    }

    public static double noise(Random random) {
        return (1 - MAX_NOISE_ON_OBJECTIVE_VALUE / 2) + MAX_NOISE_ON_OBJECTIVE_VALUE * random.nextDouble();
    }


}
