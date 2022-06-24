import model.Model;
import model.Shift;
import model.Task;
import java.util.List;
import java.util.Map;
import java.lang.Short;

public class Main {


    public static void main(String[] args, List<Task> tasks, List<Shift> shifts,  Map<Short, Shift> idsShifts) {
        Model model = new Model(tasks, shifts, idsShifts);
        Solver solver = new Solver(model);
        solver.solve();
    }
}
