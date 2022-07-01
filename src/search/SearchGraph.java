package search;

// OBS: was  ITask,  ILocation, ITimetravelmatrix
import model.Location;
import model.Task;
import model.TravelTimeMatrix;

import java.util.*;

public class SearchGraph {

    private Node origin;
    private Node destination;
    private final List<Node> nodes;
    private final Map< Task, Node> taskToNodes;
    private Double[][] travelTimeMatrix;
    private int nodeIdCounter;
    private int sourceId;
    private int sinkId;
    private int locationIdCounter;
    private final Map< Location, Integer> locationToLocationIds;


    //Trenger vi to constructors?
    public SearchGraph( TravelTimeMatrix travelTimeMatrixInput, Collection<? extends  Task> tasks,
                        Location originLocation,  Location destinationLocation) {
        this.nodes = new ArrayList<>();
        this.taskToNodes = new HashMap<>();
        this.locationToLocationIds = new HashMap<>();
        this.nodeIdCounter = 0;
        this.locationIdCounter = 0;
        this.populateGraph(travelTimeMatrixInput, tasks, originLocation, destinationLocation);
    }

    public SearchGraph(SearchGraph other) {
        this.nodeIdCounter = other.nodeIdCounter;
        this.locationIdCounter = other.locationIdCounter;
        this.sourceId = other.sourceId;
        this.sinkId = other.sinkId;
        this.nodes = new ArrayList<>();
        this.taskToNodes = new HashMap<>();
        copyNodes(other);
        this.travelTimeMatrix = new Double[other.travelTimeMatrix.length][other.travelTimeMatrix.length];
        for (int i = 0; i < travelTimeMatrix.length; i++) {
            System.arraycopy(other.travelTimeMatrix[i], 0, this.travelTimeMatrix[i], 0, other.travelTimeMatrix[i].length);
        }
        this.locationToLocationIds = new HashMap<>();
        this.locationToLocationIds.putAll(other.locationToLocationIds);
        this.origin = findEndpointNode(other.origin);
        this.destination = findEndpointNode(other.destination);
    }

    private void copyNodes(SearchGraph other) {
        for (Node node : other.nodes) {
            Node newNode;
            if (node instanceof VirtualNode) {
                newNode = new VirtualNode(node.getNodeId());
            } else {
                newNode = new Node(node);
                taskToNodes.put(node.getTask(), newNode);
            }
            nodes.add(newNode);
        }
    }

    private Node findEndpointNode(Node other) {
        for (Node node : nodes) {
            if (node.nodeId == other.nodeId) {
                if (node instanceof VirtualNode && !(other instanceof VirtualNode)) {
                    return new Node(other);
                }
                return node;
            }
        }

        throw new IllegalStateException("No destination endpoint found!");
    }


    /**
     * Location must be present in the route evaluator, i.e.,
     * the travel times matrix given when the route evaluator was constructed.
     *
     * @param originLocation The the location where the route should start.
     */
    public void updateOrigin( Location originLocation) {
        if (originLocation == null) {
            useOpenStartRoutes();
        } else if (origin instanceof VirtualNode) {
            this.origin = new Node(sourceId, null, getLocationId(originLocation));
        } else
            origin.setLocationId(locationToLocationIds.get(originLocation));
    }

    /**
     * Location must be present in the route evaluator, i.e.,
     * the travel times matrix given when the route evaluator was constructed.
     *
     * @param destinationLocation The the location where the route should end.
     */
    public void updateDestination( Location destinationLocation) {
        if (destinationLocation == null) {
            useOpenEndedRoutes();
        } else if ((destination instanceof VirtualNode)) {
            this.destination = new Node(sinkId, null, getLocationId(destinationLocation));
        } else
            destination.setLocationId(locationToLocationIds.get(destinationLocation));
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Double getTravelTime(int locationIdA, int locationIdB) {
        if (locationIdA == locationIdB){return 0.0;} // Transport from Task to Task´
        return travelTimeMatrix[locationIdA][locationIdB];
    }

    private int getNewNodeId() {
        return nodeIdCounter++;
    }

    private int getNewLocationId() {
        return locationIdCounter++;
    }

    private void populateGraph( TravelTimeMatrix travelTimeMatrixInput,
                               Collection<? extends  Task> tasks,  Location originLocation,
                                Location destinationLocation) {
        updateTravelTimeInformation(travelTimeMatrixInput);
        initializeOriginDestination(originLocation, destinationLocation);
        addNodesToGraph(tasks);
    }

    private void initializeOriginDestination( Location originLocation,  Location destinationLocation) {
        sourceId = getNewNodeId();
        sinkId = getNewNodeId();
        initializeOrigin(originLocation);
        initializeDestination(destinationLocation);
        nodes.add(origin);
        nodes.add(destination);
    }

    /**
     * Initialize the origin in the graph. If null the origin will be the first task in the route. This is represented
     * by a locationId = -1.
     *
     * @param originLocation Location of the origin, must be in the travel matrix, or null.
     */
    private void initializeOrigin( Location originLocation) {
        if (originLocation != null) {
            this.origin = new Node(sourceId, null, getLocationId(originLocation));
            locationToLocationIds.put(originLocation, origin.getLocationId());
        } else {
            this.origin = new VirtualNode(sourceId);
        }
    }

    /**
     * Initialize the destination in the graph. If null the destination will be the last task in the route. This is
     * represented by a locationId = -1.
     *
     * @param destinationLocation Location of the destination, must be in the travel matrix, or null.
     */
    private void initializeDestination( Location destinationLocation) {
        if (destinationLocation != null) {
            this.destination = new Node(sinkId, null, getLocationId(destinationLocation));
            locationToLocationIds.put(destinationLocation, destination.getLocationId());
        } else {
            this.destination = new VirtualNode(sinkId);
        }
    }

    private void addNodesToGraph(Collection<? extends  Task> tasks) {
        for ( Task task : tasks) {
            int locationId = task.getLocation() == null ? -1 : getLocationId(task.getLocation());
            Node node = new Node(getNewNodeId(), task, locationId);
            nodes.add(node);
            taskToNodes.put(task, node);
        }
    }

    private void updateTravelTimeInformation( TravelTimeMatrix travelTimeMatrixInput) {
        int n = createLocations(travelTimeMatrixInput);
        this.travelTimeMatrix = new Double[n][n];
        for ( Location locationA : travelTimeMatrixInput.getLocations()) {
            for ( Location locationB : travelTimeMatrixInput.getLocations()) {
                addTravelTime(travelTimeMatrixInput, locationA, locationB);
            }
        }
    }

    /**
     * Create all location ids.
     *
     * @param travelTimeMatrixInput Travel matrix to get locations from.
     * @return Number of locations
     */
    private int createLocations( TravelTimeMatrix travelTimeMatrixInput) {
        for ( Location location : travelTimeMatrixInput.getLocations()) {
            int locationId = getNewLocationId();
            locationToLocationIds.put(location, locationId);
        }
        return travelTimeMatrixInput.getLocations().size();
    }

    /**
     * Gets the location id of a location in the graph, the graph must contain the location.
     *
     * @param location Location to find id for
     * @return integer location id.
     */
    public int getLocationId( Location location) {
        return locationToLocationIds.get(location);
    }

    /**
     * Gets the location id of a task in the graph, the graph must contain the task.
     *
     * @param task Task to find location id for.
     * @return integer location id.
     */
    public int getLocationId( Task task) {
        return taskToNodes.get(task).getLocationId();
    }

    private void addTravelTime( TravelTimeMatrix travelTimeMatrixInput,  Location fromLocation,  Location toLocation) {
        int fromId = getLocationId(fromLocation);
        int toId = getLocationId(toLocation);

        if (!travelTimeMatrixInput.connected(fromLocation, toLocation))
            return;
        double travelTime = travelTimeMatrixInput.getTravelTime(fromLocation, toLocation);
        this.travelTimeMatrix[fromId][toId] = travelTime;
    }

    public Node getOrigin() {
        return origin;
    }

    public Node getDestination() {
        return destination;
    }

    public Node getNode( Task task) {
        return taskToNodes.get(task);
    }


    public void useOpenStartRoutes() {
        if (!(origin instanceof VirtualNode))
            origin = new VirtualNode(sourceId);
    }

    /**
     * Open ended routes ensures that the route ends at the last task in the route. Hence the route cannot have a
     * destination.
     * The destination of a route is overwritten when this is set. In the same way when the destination is updated the
     * route is no longer considered to be open ended.
     */
    public void useOpenEndedRoutes() {
        if (!(destination instanceof VirtualNode))
            destination = new VirtualNode(sinkId);
    }
}
