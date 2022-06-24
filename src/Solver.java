import algorithm.LargeNeighborhoodSearch;
import algorithm.Problem;
import model.Model;
import algorithm.Problem;
import algorithm.LargeNeighborhoodSearch;

public class Solver {

    private Model model;
    public String name;
    private Problem currentBestSoution;

    public Solver() {
    }
    public void intialize(int modelInstance) {
        this.model = new Model(4);
        model.loadData();
    }

    public void solve() {
        System.out.println("Solver running!");
        // Where do we validate solution? Do we need to send in validation function?
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(model);
        var problem = intializeLNS(model, lns);
        Problem newBestSolution = lns.solveWithConstructionHeuristic(problem);
    }

}

