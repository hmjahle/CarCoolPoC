package com.visma.of.cps.model;

import com.visma.of.api.model.Request;
import com.visma.of.api.model.RequestDepot;
import com.visma.of.api.model.RequestTask;
import com.visma.of.cps.util.Constants;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelFactory {

    //private Request request;
    private int numberOfTasks;
    private int returnTime;

    private List<Task> taskList;
    private List<Visit> visitList;

    private Map<Integer, Location> locations;

    private Map<Integer, TravelTimeMatrix> travelTimeMatrix;


    public ModelFactory(Request request){
        //this.request = request;
        this.taskList = new ArrayList<>();
        this.locations = new HashMap<>();
        initializeModel(request);
    }

    private void initializeModel(Request request){
        this.numberOfTasks = request.getTasks().size();
        createDepot(request.getDepot());
        createTasks(request.getTasks());
        setTravelTime(request);
        // create shifts
        // set who is mototrized and carpoolable
        // acctually create the model. probably make new constructor in model
    }

    private void createDepot(RequestDepot depot){
        Location depop = new Location(0, depot.getxCoord(), depot.getyCoord());
        this.returnTime = depot.getReturnTime();
        locations.put(0, depop);
    }

    private void createTasks(List<RequestTask> requestTasks){
        for (RequestTask requestTask : requestTasks){
            createTask(requestTask);
        }
    }

    private void createTask(RequestTask requestTask){
        Task task = new Task(requestTask.getId()-1, numberOfTasks);
        task.setDuration(requestTask.getCareTime());
        task.setWeight(requestTask.getDemand());
        task.setStartTime(requestTask.getStartTime());
        task.setEndTime(requestTask.getEndTime());

        setLocation(task, requestTask);
        createVisits(task);

        taskList.add(task);
    }

    private void setLocation(Task task, RequestTask requestTask){
        Location location = new Location(task.getId(), requestTask.getxCoord(), requestTask.getyCoord());
        task.setLocation(location);
        locations.put(task.getId(), location);
    }

    private void createVisits(Task task){
        Visit visit1 = new Visit((task.getId()), task, Constants.VisitType.COMPLETE_TASK);
        Visit visit1Virtual= new Visit((task.getId() + 2 * numberOfTasks), task, Constants.VisitType.JOIN_MOTORIZED);
        Visit visit2 = new Visit((task.getId() + numberOfTasks), task, Constants.VisitType.DROP_OF);
        Visit visit2Virtual = new Visit((task.getId() + 3 * numberOfTasks), task, Constants.VisitType.PICK_UP);

        this.visitList.add(visit1);
        this.visitList.add(visit1Virtual);
        this.visitList.add(visit2);
        this.visitList.add(visit2Virtual);
    }

    private void setTravelTime(Request request){
        if (this.taskList.isEmpty()){
            throw new IllegalCallerException("Model class is missing task-data from file");
        }
        Map<Integer, TravelTimeMatrix> ogTravelTimes = new HashMap<Integer, TravelTimeMatrix>();
        List<List<Object>> requestTravelTimes  = request.getTravelTimes();
        Map<Location, Map<Location, Integer>> drivingTimes = new HashMap<Location,Map<Location,Integer>>();
        Map<Location, Map<Location, Integer>> walkingTimes = new HashMap<Location, Map<Location, Integer>>();
        for (int i = 0; i <requestTravelTimes.size(); i++){
            List<Object> indTravelTimes = requestTravelTimes.get(i);
            Map<Location, Integer> indDrivingTimes = new HashMap<Location, Integer>();
            Map<Location, Integer> indWalkingTimes = new HashMap<Location, Integer>();
            for (int j = 0; j < indTravelTimes.size(); j++){
                if (indTravelTimes.get(j) instanceof Long){
                    indDrivingTimes.put(this.locations.get(j), ((Long) indTravelTimes.get(j)).intValue());
                    indWalkingTimes.put(this.locations.get(j), (((Long) indTravelTimes.get(j)).intValue())*10); // Ganger med 10 fordi walk
                    // System.out.println(i + " " +this.locations.get(j).getId()+ " " + ((Long) indJsonTravelTimes.get(j)).doubleValue());
                }
                else {
                    indDrivingTimes.put(this.locations.get(j), (Integer) indTravelTimes.get(j));
                    indWalkingTimes.put(this.locations.get(j), ((Integer) indTravelTimes.get(j))*10); // samme her, ganger med 10
                    //System.out.println(i + " " +this.locations.get(j).getId()+ " " + (Double) indJsonTravelTimes.get(j));
                }
            }
            drivingTimes.put(this.locations.get(i), indDrivingTimes);
            walkingTimes.put(this.locations.get(i), indWalkingTimes);
        }
        TravelTimeMatrix drivingMatrix = new TravelTimeMatrix(drivingTimes);
        TravelTimeMatrix walkingMatrix = new TravelTimeMatrix(walkingTimes);
        ogTravelTimes.put(Constants.TransportMode.DRIVE, drivingMatrix);
        ogTravelTimes.put(Constants.TransportMode.WALK, walkingMatrix);
        this.travelTimeMatrix = ogTravelTimes;
    }
}
