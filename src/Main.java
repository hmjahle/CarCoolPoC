import model.Model;

public class Main {


    public static void main(String[] args) {
        Model model = new Model();

        Solver solver = new Solver(model);
        solver.solve();
    }
}
