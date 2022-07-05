package routeEvaluator.solver.algorithm;

import java.util.Enumeration;

/**
 * This class implement the IExtendInfo, it has two elements.
 */
public class ExtendInfoOneElement implements IExtendInfo {

    NodeList nodeListOne;
    Node node;

    public ExtendInfoOneElement(NodeList nodeListOne) {
        this.nodeListOne = nodeListOne;
    }

    /**
     * Check if there is a node that corresponds to the resource count, if not it
     * can be interpreted as if all nodes on the nodeListOne has been visited.
     *
     * @param label Label to extend.
     * @return Enumeration.
     */
    @Override
    public Enumeration<ExtendToInfo> extend(Label label) {
        node = nodeListOne.getNode(label.getResources().getElementOneCount());
        return new ExtendInfoOneElementEnumerator();
    }

    /**
     * Creates an resource with one element, corresponding to one nodeList. This points to the first node
     * on the first nodeList.
     *
     * @return ResourceOneElement.
     */
    @Override
    public IResource createEmptyResource() {
        return new ResourceOneElement(0);
    }

    /**
     * An private implementation of enumeration. If there is a node to extend to,
     * then ExtendInfo is returned for that node. There can only be one or zero elements.
     */
    private class ExtendInfoOneElementEnumerator implements Enumeration<ExtendToInfo> {

        boolean next = (node != null);

        @Override
        public boolean hasMoreElements() {
            return next;
        }

        @Override
        public ExtendToInfo nextElement() {
            next = false;
            return new ExtendToInfo(node, 1);
        }
    }
}
