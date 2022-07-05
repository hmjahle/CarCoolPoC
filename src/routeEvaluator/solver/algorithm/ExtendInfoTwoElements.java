package routeEvaluator.solver.algorithm;

import java.util.Enumeration;

/**
 * This class implement the IExtendInfo, it has two elements.
 */
public class ExtendInfoTwoElements implements IExtendInfo {

    NodeList nodeListOne;
    NodeList nodeListTwo;
    ExtendToInfo[] extendToInfo = new ExtendToInfo[2];
    int size;

    public ExtendInfoTwoElements(NodeList nodeListOne, NodeList nodeListTwo) {
        this.nodeListOne = nodeListOne;
        this.nodeListTwo = nodeListTwo;
    }

    /**
     * Check if there are nodes that corresponds to the resource counts, if not it
     * can be interpreted as if all nodes on the nodeLists has been visited.
     *
     * @param label Label to extend.
     * @return Enumeration.
     */
    @Override
    public Enumeration<ExtendToInfo> extend(Label label) {
        ResourceTwoElements resourceTwoElements = (ResourceTwoElements) label.getResources();
        Node nodeOne = nodeListOne.getNode(resourceTwoElements.getElementOneCount());
        Node nodeTwo = nodeListTwo.getNode(resourceTwoElements.getElementTwoCount());
        if (nodeOne != null) {
            extendToInfo[0] = new ExtendToInfo(nodeOne, 1);
            size = 1;
        } else {
            size = 0;
        }
        if (nodeTwo != null) {
            extendToInfo[size++] = new ExtendToInfo(nodeTwo, 2);
        }

        return new ExtendInfoTwoElementsEnumerator();
    }

    /**
     * Creates an resource with two elements, corresponding to nodeLists one and two.
     * These points to the first nodes on  both lists.
     *
     * @return ResourceTwoElements.
     */
    @Override
    public IResource createEmptyResource() {
        return new ResourceTwoElements(0, 0);
    }

    /**
     * An private implementation of enumeration. If there is one or two nodes to extend to,
     * then ExtendInfo is returned for those nodes. There can only be one, two or zero elements.
     */
    private class ExtendInfoTwoElementsEnumerator implements Enumeration<ExtendToInfo> {

        int next;

        @Override
        public boolean hasMoreElements() {
            return next != size;
        }

        @Override
        public ExtendToInfo nextElement() {
            return extendToInfo[next++];
        }
    }
}
