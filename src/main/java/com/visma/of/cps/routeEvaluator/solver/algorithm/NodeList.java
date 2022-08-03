package com.visma.of.cps.routeEvaluator.solver.algorithm;

import com.visma.of.cps.model.Visit;

import java.util.List;

public class NodeList {
    private final Node[] nodes;
    private int nodesCnt;

    public NodeList(int size) {
        nodes = new Node[size];
        nodesCnt = 0;
    }

    public Node getNode(int i) {
        return i > nodesCnt - 1 ? null : nodes[i];
    }

    public int size() {
        return nodesCnt;
    }

    public void clear() {
        nodesCnt = 0;
    }

    /**
     * Insert the nodes representing the visits in the search graph. The visits must have a node representing them in
     * the graph provided.
     *
     * @param graph Graph from which the nodes should be found.
     * @param visit Visit to be inserted.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Visit> visits) {
        nodesCnt = visits.size();
        for (int i = 0; i < visits.size(); i++) {
            nodes[i] = graph.getNode(visits.get(i));
        }
    }

    /**
     * Insert the nodes representing the visits in the search graph. The visits must have a node representing them in
     * the graph provided. A task single in the list at a specific index is skipped.
     *
     * @param graph           Graph from which the nodes should be found.
     * @param visits          Visit to be inserted.
     * @param skipTaskAtIndex Index at which the task should be skipped.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Visit> visits, int skipVisitAtIndex) {
        nodesCnt = visits.size() - 1;
        int insertAtIndex = 0;
        for (int i = 0; i < visits.size(); i++) {
            if (i == skipVisitAtIndex)
                continue;
            nodes[insertAtIndex] = graph.getNode(visits.get(i));
            insertAtIndex++;
        }
    }

    /**
     * Insert the nodes representing the visits in the search graph. The visits must have a node representing them in
     * the graph provided. Tasks in the list at specific indices are skipped.
     *
     * @param graph              Graph from which the nodes should be found.
     * @param visits              Tasks to be inserted.
     * @param skipVisitsAtIndices Indices at which the task should be skipped, the list must be ordered increasing.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Visit> visits, List<Integer> skipVisitsAtIndices) {
        nodesCnt = visits.size() - skipVisitsAtIndices.size();
        int insertAtIndex = 0;
        int skipped = 0;
        int skipValue = getSkipValue(skipVisitsAtIndices, skipped);
        for (int i = 0; i < visits.size(); i++) {
            if (i == skipValue) {
                skipped++;
                skipValue = getSkipValue(skipVisitsAtIndices, skipped);
                continue;
            }
            nodes[insertAtIndex] = graph.getNode(visits.get(i));
            insertAtIndex++;
        }
    }

    private int getSkipValue(List<Integer> skipVisitsAtIndices, int skipped) {
        return skipped < skipVisitsAtIndices.size() ? skipVisitsAtIndices.get(skipped) : -1;
    }


    public void initializeWithNode(SearchGraph graph, Visit task) {
        nodesCnt = 1;
        nodes[0] = graph.getNode(task);
    }

    public void addNode(SearchGraph graph, Visit task) {
        nodes[nodesCnt] = graph.getNode(task);
        nodesCnt++;
    }
}
