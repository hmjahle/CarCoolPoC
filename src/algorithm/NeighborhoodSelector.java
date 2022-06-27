package algorithm;

import algorithm.operators.IDestroyOperator;
import algorithm.operators.IRepairOperator;

import java.util.*;

public class NeighborhoodSelector {

    private Random random;
    private Set<IDestroyOperator> destroyOperators;
    private Set<IRepairOperator> repairOperators;

    private List<INeighborhoodMove> neighborhoodMoves;
    private Map<INeighborhoodMove, Integer> success;
    private Map<INeighborhoodMove, Integer> failure;
    private int totalTrials = 0;

    public NeighborhoodSelector() {
        this.destroyOperators = new HashSet<>();
        this.repairOperators = new HashSet<>();
        this.neighborhoodMoves = new ArrayList<>();
        this.random = new Random();
        success = new HashMap<>();
        failure = new HashMap<>();

    }

        /**
     * Add a neighborhood operator to the lns. For destroy/repair operators, when added all combinations with opposite
     * destroy/repair operators are added to the list of neighborhood moves such that this list always maintain all
     * combinations of destroy / repair operators.
     *
     * @param neighborhoodOperator Neighborhood operator to add.
     */
    public void addNeighborhood(Object neighborhoodOperator) {
/*         if (neighborhoodOperator instanceof IImproveOperator) {
            addImproveOperator((IImproveOperator) neighborhoodOperator);
        } else  */
        if (neighborhoodOperator instanceof IDestroyOperator) {
            addDestroyOperator((IDestroyOperator) neighborhoodOperator);
        } else if (neighborhoodOperator instanceof IRepairOperator) {
            addRepairOperator((IRepairOperator) neighborhoodOperator);
        }
    }

    private void addRepairOperator(IRepairOperator repairOperator) {
        for (IDestroyOperator destroyOperator : destroyOperators) {
            INeighborhoodMove neighborhoodMove = new NeighborhoodDestroyRepairMove(destroyOperator, repairOperator);
            initiateSuccessFailureMaps(neighborhoodMove);
            neighborhoodMoves.add(neighborhoodMove);
        }
        repairOperators.add(repairOperator);
    }

    private void initiateSuccessFailureMaps(INeighborhoodMove neighborhoodMove) {
        success.put(neighborhoodMove, 0);
        failure.put(neighborhoodMove, 0);
    }

    private void addDestroyOperator(IDestroyOperator destroyOperator) {
        for (IRepairOperator repairOperator : repairOperators) {
            INeighborhoodMove neighborhoodMove = new NeighborhoodDestroyRepairMove(destroyOperator, repairOperator);
            initiateSuccessFailureMaps(neighborhoodMove);
            neighborhoodMoves.add(neighborhoodMove);
        }
        destroyOperators.add(destroyOperator);
    }

    /**
     * Choose a neighborhood and applies it to the given solution.
     *
     * @param problem Contains the solution, its constraints and objectives. It is this solution that is changed by the
     *                selected neighborhood move. The solution provided MUST be based on the same model as the LNS and
     *                the neighborhood operators within. Note that the provided solution WILL be altered by the operators.
     * @return Result of the neighborhood, containing the delta value if one choose to accept and an altered version of
     * the provided solution information.
     */
    public NeighborhoodMoveInfo applyRandomNeighborhood(Problem problem) {
        INeighborhoodMove neighborhoodMove = getRandomNeighborhoodMove();
        return neighborhoodMove.apply(problem);
    }

    /**
     * Choose a uniformly random neighborhood and returns it. Assumes that at least one exist.
     *
     * @return A neighborhood move.
     */
    public INeighborhoodMove getRandomNeighborhoodMove() {
        int randomNumber = random.nextInt(neighborhoodMoves.size());
        return neighborhoodMoves.get(randomNumber);
    }


    
}
