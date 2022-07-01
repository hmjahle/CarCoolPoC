package search;

import model.Task;

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
     * Insert the nodes representing the tasks in the search graph. The tasks must have a node representing them in
     * the graph provided.
     *
     * @param graph Graph from which the nodes should be found.
     * @param tasks Tasks to be inserted.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Task> tasks) {
        nodesCnt = tasks.size();
        for (int i = 0; i < tasks.size(); i++) {
            nodes[i] = graph.getNode(tasks.get(i));
        }
    }

    /**
     * Insert the nodes representing the tasks in the search graph. The tasks must have a node representing them in
     * the graph provided. A task single in the list at a specific index is skipped.
     *
     * @param graph           Graph from which the nodes should be found.
     * @param tasks           Tasks to be inserted.
     * @param skipTaskAtIndex Index at which the task should be skipped.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Task> tasks, int skipTaskAtIndex) {
        nodesCnt = tasks.size() - 1;
        int insertAtIndex = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (i == skipTaskAtIndex)
                continue;
            nodes[insertAtIndex] = graph.getNode(tasks.get(i));
            insertAtIndex++;
        }
    }

    /**
     * Insert the nodes representing the tasks in the search graph. The tasks must have a node representing them in
     * the graph provided. Tasks in the list at specific indices are skipped.
     *
     * @param graph              Graph from which the nodes should be found.
     * @param tasks              Tasks to be inserted.
     * @param skipTasksAtIndices Indices at which the task should be skipped, the list must be ordered increasing.
     */
    public void initializeWithNodes(SearchGraph graph, List<? extends Task> tasks, List<Integer> skipTasksAtIndices) {
        nodesCnt = tasks.size() - skipTasksAtIndices.size();
        int insertAtIndex = 0;
        int skipped = 0;
        int skipValue = getSkipValue(skipTasksAtIndices, skipped);
        for (int i = 0; i < tasks.size(); i++) {
            if (i == skipValue) {
                skipped++;
                skipValue = getSkipValue(skipTasksAtIndices, skipped);
                continue;
            }
            nodes[insertAtIndex] = graph.getNode(tasks.get(i));
            insertAtIndex++;
        }
    }

    private int getSkipValue(List<Integer> skipTasksAtIndices, int skipped) {
        return skipped < skipTasksAtIndices.size() ? skipTasksAtIndices.get(skipped) : -1;
    }


    public void initializeWithNode(SearchGraph graph, Task task) {
        nodesCnt = 1;
        nodes[0] = graph.getNode(task);
    }

    public void addNode(SearchGraph graph, Task task) {
        nodes[nodesCnt] = graph.getNode(task);
        nodesCnt++;
    }
}
