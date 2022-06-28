package model;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.lang.Short;
import java.util.Iterator;
import java.util.List;

public class Model {
    // Stuff that goes in the model here
    private final String filePath;
    private JSONObject data;
    private Collection<Task> tasks;
    private int return_time;
    private Map<Integer, Location> locations = new HashMap<Integer,Location>();
    private Map<Short, Shift> idsShifts; // denne trenger vi egentlig ikke her fordi sykepleierne er homogene
    private Map<Integer, TravelTimeMatrix> travelTimeMatrix;
    private Collection<Visit> visits;
    private List<Shift> shifts;

    public Model(int modelInstance) {
        this.filePath = System.getProperty("user.dir") + "/resources/train_" + modelInstance + ".json";
        tasks = new ArrayList<>();
        shifts = new ArrayList<>();
        visits = new ArrayList<>();
    }

    public List<Shift> getShifts() {
        return this.shifts;
    }

    public Map<Short, Shift> getIdsShifts(){ return this.idsShifts;}

    public Map<Integer, TravelTimeMatrix> getTravelTimeMatrix(){ return this.travelTimeMatrix; }

    public void loadData() {

        JSONParser parser = new JSONParser();

        try (
            FileReader reader = new FileReader(this.filePath)
        ) {
            JSONObject data = (JSONObject) parser.parse(reader);
            this.data = data;
            int numWorkers =  Integer.parseInt(Long.toString((Long) data.get("nbr_nurses")));
            for(int i = 0; i < numWorkers; i ++) {
                shifts.add(new Shift(i));
            }

            JSONObject patients = (JSONObject) data.get("patients");
            Iterator keys  = patients.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                if (patients.get(key) instanceof JSONObject) {
                    Task task = new Task((short) (Integer.parseInt((String) key) - 1), (short) patients.size());

                    // Creating tasks
                    tasks.add(task);

                    // Creating corresponding visits
                    Visit visit1 = new Visit((short) (Integer.parseInt((String) key) - 1), task);
                    Visit visit1Virtual= new Visit((short) (Integer.parseInt((String) key) - 1 + 2 * patients.size()), task);
                    Visit visit2 = new Visit((short) (Integer.parseInt((String) key) - 1 + patients.size()), task);
                    Visit visit2Virtual = new Visit((short) (Integer.parseInt((String) key) - 1 + 3 * patients.size()), task);

                    this.visits.add(visit1);
                    this.visits.add(visit1Virtual);
                    this.visits.add(visit2);
                    this.visits.add(visit2Virtual);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setTasks(){
        if (this.data.isEmpty()){
            throw new IllegalCallerException("Model class is missing data from file");
        }
        Collection<Task> allTasks = new ArrayList<Task>();
        JSONObject jsonTasks = (JSONObject) this.data.get("patients");
        for (int i = 1; i < jsonTasks.size()+1; i++){
            Task task = new Task(i);
            JSONObject jsonAttributes = (JSONObject) jsonTasks.get(Integer.toString(i));
            task.setDuration((Long) jsonAttributes.get("care_time"));
            task.setWeight((Long) jsonAttributes.get("demand"));
            task.setStartTime((Long) jsonAttributes.get("start_time"));
            task.setEndTime((Long) jsonAttributes.get("end_time"));
            Location location = new Location(i, (Long) jsonAttributes.get("x_coord"), (Long) jsonAttributes.get("x_coord"));
            locations.put(i, location);
            task.setLocation(location);
            allTasks.add(task);
        }
        this.tasks = allTasks;
        // Legge til depop i locations, med indeks 0
        JSONObject jsonDepop = (JSONObject) this.data.get("depot");
        Location depop = new Location(0, (Long) jsonDepop.get("x_coord"), (Long) jsonDepop.get("x_coord"));
        locations.put(0, depop);
    
    }
    public void setTravelTime(){
        if (this.tasks.isEmpty()){
            throw new IllegalCallerException("Model class is missing task-data from file");
        }
        Map<Integer, TravelTimeMatrix> ogTravelTimes = new HashMap<Integer, TravelTimeMatrix>();
        JSONArray jsonDrivingTimes = (JSONArray) this.data.get("travel_times");
        Map<Location, Map<Location, Double>> drivingTimes = new HashMap<Location,Map<Location,Double>>();
        Map<Location, Map<Location, Double>> walkingTimes = new HashMap<Location, Map<Location, Double>>();    
        for (int i = 0; i <jsonDrivingTimes.size(); i++){
            JSONArray indJsonTravelTimes = (JSONArray) jsonDrivingTimes.get(i);
            Map<Location, Double> indDrivingTimes = new HashMap<Location, Double>();
            Map<Location, Double> indWalkingTimes = new HashMap<Location, Double>();
            for (int j = 0; j < indJsonTravelTimes.size(); j++){
                if (indJsonTravelTimes.get(j) instanceof Long){
                    indDrivingTimes.put(this.locations.get(j), ((Long) indJsonTravelTimes.get(j)).doubleValue());
                    indWalkingTimes.put(this.locations.get(j), (((Long) indJsonTravelTimes.get(j)).doubleValue())*10); // Ganger med 10 fordi walk
                    // System.out.println(i + " " +this.locations.get(j).getId()+ " " + ((Long) indJsonTravelTimes.get(j)).doubleValue());
                }
                else {
                    indDrivingTimes.put(this.locations.get(j), (Double) indJsonTravelTimes.get(j));
                    indWalkingTimes.put(this.locations.get(j), ((Double) indJsonTravelTimes.get(j))*10); // samme her, ganger med 10
                    //System.out.println(i + " " +this.locations.get(j).getId()+ " " + (Double) indJsonTravelTimes.get(j));
                }
            }
            drivingTimes.put(this.locations.get(i), indDrivingTimes);
            walkingTimes.put(this.locations.get(i), indWalkingTimes);
        }
        TravelTimeMatrix drivingMatrix = new TravelTimeMatrix(drivingTimes);
        TravelTimeMatrix walkingMatrix = new TravelTimeMatrix(walkingTimes);
        ogTravelTimes.put(0, drivingMatrix);
        ogTravelTimes.put(1, walkingMatrix);
        this.travelTimeMatrix = ogTravelTimes;
    }

    public static void main(String[] args) {
        Model model = new Model(4);
        model.loadData();
        model.setTasks();
        model.setTravelTime();
        for (int i = 0; i<model.locations.size();i++){System.out.println(model.travelTimeMatrix.get(0).getTravelTimes().get(model.locations.get(0)).get(model.locations.get(i)));}
        // System.out.println(model.travelTimeMatrix.get(0).getTravelTimes().get(model.locations.get(1)));
        // for (int i = 1; i<model.locations.size()+1;i++){ System.out.println(model.locations.get(i));}
        // for (int i = 1; i < model.travelTimeMatrix.get(0).getTravelTimes().size();i++){ System.out.println(model.travelTimeMatrix.get(0).getTravelTimes().get(model.locations.get(i))); }
        // for (Iterator<Task> iterator = model.tasks.iterator(); iterator.hasNext();){System.out.println(iterator.next().getStartTime());}
    }
} 
