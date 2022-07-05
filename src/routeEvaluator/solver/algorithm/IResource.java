package routeEvaluator.solver.algorithm;

/**
 * The ResourceInterface is used when extending labels. It must be able to dominated another resource of the same
 * implementation.
 */
public interface IResource {

    /**
     * The dominate function returns: -1 if this dominates other, 0 if equal, 1 if other dominates this and
     * 2 if they are not equal and neither dominates the other
     *
     * @param other Resource that is compared.
     * @return integer.
     */
    int dominates(IResource other);

    /**
     * Returns a new resource based on the extend info, that holds information on which nodeSet it belongs to.
     *
     * @param extendToInfo The information needed to generate the new resource.
     * @return New resource.
     */
    IResource extend(ExtendToInfo extendToInfo);

    int getElementOneCount();

    int getElementTwoCount();
}
