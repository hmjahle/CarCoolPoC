package routeEvaluator.solver.algorithm;

/**
 * This class implement IResource, and has two elements. This should be used by INodeExtendSets with to elements.
 */
public class ResourceTwoElements implements IResource {

    private int elementOneCount;
    private int elementTwoCount;

    public ResourceTwoElements(int elementOneCount, int elementTwoCount) {
        this.elementOneCount = elementOneCount;
        this.elementTwoCount = elementTwoCount;
    }

    @Override
    public ResourceTwoElements extend(ExtendToInfo extendToInfo) {
        if (extendToInfo.getExtendNodeSetNumber() == 1)
            return new ResourceTwoElements(elementOneCount + 1, elementTwoCount);
        else if (extendToInfo.getExtendNodeSetNumber() == 2)
            return new ResourceTwoElements(elementOneCount, elementTwoCount + 1);
        else
            return new ResourceTwoElements(elementOneCount, elementTwoCount);
    }

    public int getElementOneCount() {
        return elementOneCount;
    }

    public int getElementTwoCount() {
        return elementTwoCount;
    }

    /**
     * Implements the dominates function see the IResource.
     * @param other Resource to check domination towards.
     * @return  See the interface documentation for the return values.
     */
    @Override
    public int dominates(IResource other) {

        //Equals
        if (this.elementOneCount == other.getElementOneCount() && this.elementTwoCount == other.getElementTwoCount()) {
            return 0;
        }
        //Other dominates
        else if (isEqualOrBetterOnBothElements(this,other)) {
            return 1;
        }
        //This dominates other
        if (isEqualOrBetterOnBothElements(other,this)) {
            return -1;
        }
        //Neither dominates
        return 2;
    }

    private boolean isEqualOrBetterOnBothElements(IResource me,IResource other) {
        return other.getElementOneCount() >= me.getElementOneCount() && other.getElementTwoCount() >= me.getElementTwoCount();
    }

    @Override
    public String toString() {
        return "ElementOneCount = " + elementOneCount + ", ElementTwoCount = " + elementTwoCount;
    }
}
