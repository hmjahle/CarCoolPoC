package search;

// OBS: was  ITask,  ILocation, ITimetravelmatrix
import model.Location;
import model.TravelTimeMatrix;
import model.Visit;
import util.Constants;

import java.util.*;

public class SearchGraph {

    private Node origin;
    private Node destination;
    private final List<Node> nodes;
    private final Map< Visit, Node> visitToNodes;
    private Map<Integer, Integer[][]> travelTimeMatrix;
    private int nodeIdCounter;
    private int sourceId;
    private int sinkId;


    //Trenger vi to constructors?
    public SearchGraph( Map<Integer, TravelTimeMatrix>  travelTimeMatrixInput, Collection<? extends  Visit> visits,
                        Location originLocation,  Location destinationLocation) {
        this.nodes = new ArrayList<>();
        this.visitToNodes = new HashMap<>();
        this.nodeIdCounter = 0;
        this.populateGraph(travelTimeMatrixInput, visits, originLocation, destinationLocation);
    }

    public SearchGraph(SearchGraph other) {
        this.nodeIdCounter = other.nodeIdCounter;
        this.sourceId = other.sourceId;
        this.sinkId = other.sinkId;
        this.nodes = new ArrayList<>();
        this.visitToNodes = new HashMap<>();
        copyNodes(other);
        int walk = Constants.TransportMode.WALK;
        int drive = Constants.TransportMode.DRIVE;
        Integer[][] travelTimeMatrixWalk = new Integer[other.travelTimeMatrix.get(walk).length][other.travelTimeMatrix.get(walk).length];
        for (int i = 0; i < travelTimeMatrixWalk.length; i++) {
            System.arraycopy(other.travelTimeMatrix.get(walk)[i], 0, this.travelTimeMatrix.get(walk)[i], 0, other.travelTimeMatrix.get(walk)[i].length);
        }
        Integer[][] travelTimeMatrixDrive = new Integer[other.travelTimeMatrix.get(drive).length][other.travelTimeMatrix.get(drive).length];
        for (int i = 0; i < travelTimeMatrix.get(drive).length; i++) {
            System.arraycopy(other.travelTimeMatrix.get(drive)[i], 0, this.travelTimeMatrix.get(drive)[i], 0, other.travelTimeMatrix.get(drive)[i].length);
        }
        this.travelTimeMatrix.put(Constants.TransportMode.WALK, travelTimeMatrixWalk);
        this.travelTimeMatrix.put(Constants.TransportMode.DRIVE, travelTimeMatrixDrive);
        this.origin = other.origin;
        this.destination = other.destination;
    }

    private void copyNodes(SearchGraph other) {
        for (Node node : other.nodes) {
            Node newNode = new Node(node);
            if (!node.isDepotNode()) {
                visitToNodes.put(node.getVisit(), newNode);
            }
            nodes.add(newNode);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getTravelTime(int locationIdA, int locationIdB, int transportmode) {
        if (locationIdA == locationIdB){return 0;} // Transport from Task to Task´
        return travelTimeMatrix.get(transportmode)[locationIdA][locationIdB];
    }

    private int getNewNodeId() {
        return nodeIdCounter++;
    }

    private void populateGraph( Map<Integer, TravelTimeMatrix>  travelTimeMatrixInput,
                               Collection<? extends  Visit> visits,  Location originLocation,
                                Location destinationLocation) {
        updateTravelTimeInformation(travelTimeMatrixInput);
        initializeOriginDestination(originLocation, destinationLocation);
        addNodesToGraph(visits);
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

     *
     * Removed virtual node. If locationId is 0 it should be the last or first task.
     * 
     * @param originLocation Location of the origin, must be in the travel matrix, or null.
     */
    private void initializeOrigin(Location originLocation) {
        this.origin = new Node(sourceId, null);
    }

    /**
     * Initialize the destination in the graph. If null the destination will be the last task in the route. This is
     * represented by a locationId = 0. OBS!! was id=-1, but removed virtual node. If locationId is 0 it should be the last task.
     * 
     *
     * @param destinationLocation Location of the destination, must be in the travel matrix, or null.
     */
    private void initializeDestination( Location destinationLocation) {
        this.destination = new Node(sinkId, null);
    }

    private void addNodesToGraph(Collection<? extends  Visit> visits) {
        for ( Visit visit : visits) {
            Node node = new Node(getNewNodeId(), visit);
            nodes.add(node);
            visitToNodes.put(visit, node);
        }
    }

    private void updateTravelTimeInformation( Map<Integer, TravelTimeMatrix> travelTimeMatrixInput) {
        TravelTimeMatrix travelTimeMatrixInputDrive = travelTimeMatrixInput.get(Constants.TransportMode.DRIVE);
        TravelTimeMatrix travelTimeMatrixInputWalk = travelTimeMatrixInput.get(Constants.TransportMode.WALK);

        int n = travelTimeMatrixInputDrive.getLocations().size();
        int m = travelTimeMatrixInputWalk.getLocations().size();

        this.travelTimeMatrix = new HashMap<Integer, Integer[][]>(){{
            put(Constants.TransportMode.DRIVE, new Integer[n][n]);
            put(Constants.TransportMode.WALK, new Integer[m][m]);
        }};
        // Is the same locations in both, so whould not matter which we iterate through
        for ( Location locationA : travelTimeMatrixInputDrive.getLocations()) {
            for ( Location locationB : travelTimeMatrixInputDrive.getLocations()) {
                addTravelTime(travelTimeMatrixInputDrive, locationA, locationB, Constants.TransportMode.DRIVE);
                addTravelTime(travelTimeMatrixInputWalk, locationA, locationB, Constants.TransportMode.WALK);
            }
        }
    }


    /**
     * Gets the location id of a location in the graph, the graph must contain the location.
     *
     * @param location Location to find id for
     * @return integer location id.
     */
    public int getLocationId( Location location) {
        return location.getId();
    }

    /**
     * Gets the location id of a task in the graph, the graph must contain the task.
     *
     * @param task Task to find location id for.
     * @return integer location id.
     */
    public int getLocationId(Visit visit) {
        return visitToNodes.get(visit).getLocationId();
    }

    private void addTravelTime(TravelTimeMatrix travelTimeMatrixInput,  Location fromLocation,  Location toLocation, int transportmode) {
        int fromId = getLocationId(fromLocation);
        int toId = getLocationId(toLocation);
        if (!travelTimeMatrixInput.connected(fromLocation, toLocation))
            return;
        int travelTime = travelTimeMatrixInput.getTravelTime(fromLocation, toLocation);
        this.travelTimeMatrix.get(transportmode)[fromId][toId] = travelTime;
    }

    public Node getOrigin() {
        return origin;
    }

    public Node getDestination() {
        return destination;
    }

    public Node getNode(Visit visit) {
        return visitToNodes.get(visit);
    }

}
