package routeEvaluator.solver.algorithm;

import java.util.Arrays;
import java.util.List;

/**
 * The label lists class is used within the labelling algorithm to handle labels on the node.
 * The class handles the labels on the nodes, when new labels are considered to be added it is checked whether
 * they are dominated by existing labels. If they are, they are not added otherwise they are added.
 * If they dominate other labels those labels are removed from the list.
 */
public class LabelLists {
    private static final int DEFAULT_CAPACITY = 10;

    private final Label[][] elements;
    private final int[] elementCnt;

    public LabelLists(int nodes) {
        this(nodes, DEFAULT_CAPACITY);
    }

    public LabelLists(int nodes, int labelCapacity) {
        this.elements = new Label[nodes][labelCapacity];
        this.elementCnt = new int[nodes];
    }

    /**
     * Attempts to add a label to a node, if the label is added, i.e., not dominated by and existing
     * label it returns true, otherwise false.
     *
     * @param node  The node to which the label should be added.
     * @param label The label to be added.
     * @return True if the label is added, otherwise false.
     */
    public boolean addLabelOnNode(Node node, Label label) {
        int nodeId = node.getNodeId();
        if (canNotBeAdded(label, nodeId)) {
            return false;
        }

        updateArrayLength(nodeId);
        elements[nodeId][elementCnt[nodeId]] = label;
        elementCnt[nodeId]++;

        return true;
    }

    /**
     * Check if it is possible to add the label, i.e., if it is dominated.
     * At the same time labels that it might dominate are deleted.
     *
     * @param label  Label to be added.
     * @param nodeId Node to add the label to.
     * @return True if the label can be added to the node, otherwise false.
     */
    private boolean canNotBeAdded(Label label, int nodeId) {
        int labelCnt = elementCnt[nodeId];
        Label[] labelsOnNode = elements[nodeId];

        int i = 0;
        while (i < labelCnt) {
            int dominates = labelsOnNode[i].dominates(label);
            if (dominates <= 0) {
                // not possible to add as it is dominated
                return true;
            }

            if (dominates == 1) {
                // remove dominated label
                labelsOnNode[i].close();
                labelsOnNode[i] = labelsOnNode[labelCnt - 1];
                labelsOnNode[labelCnt - 1] = null;
                labelCnt--;
                continue;
            }

            i++;
        }
        elementCnt[nodeId] = labelCnt;
        return false;
    }

    /**
     * Check if there is enough capacity in the label list for the given node.
     * If not the array capacity is doubled.
     *
     * @param nodeId Node to check.
     */
    private void updateArrayLength(int nodeId) {
        if (elementCnt[nodeId] + 1 >= elements[nodeId].length) {
            elements[nodeId] = Arrays.copyOf(elements[nodeId], increasedCapacity(elements[nodeId].length));
        }
    }

    private int increasedCapacity(int oldCapacity) {
        return oldCapacity + (oldCapacity << 1);
    }

    public List<Label> findLabels(Node node) {
        return Arrays.asList(elements[node.getNodeId()]).subList(0, elementCnt[node.getNodeId()]);
    }

    public int size(Node node) {
        return elementCnt[node.getNodeId()];
    }

    public void clear() {
        for (int i = 0; i < elementCnt.length; i++) {
            for (int j = 0; j < elementCnt[i]; j++) {
                elements[i][j] = null;
            }
            elementCnt[i] = 0;
        }
    }

    public int getLabelCapacity(Node node) {
        return elements[node.getNodeId()].length;
    }
}