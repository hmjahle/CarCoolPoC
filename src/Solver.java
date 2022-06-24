import model.Model;

public class Solver {

    private Model model;
    public String name;

    public Solver(Model model) {
        name = "VRPTRWCP";
        this.model = model;
    }

    public void solve() {
        System.out.println("Solver running!");
    }

}
