package model;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Model {
    // Stuff that goes in the model here
    private final String filePath;
    private Collection<Task> tasks;
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

    public void loadData() {

        JSONParser parser = new JSONParser();

        try (
            FileReader reader = new FileReader(this.filePath)
        ) {

            JSONObject data = (JSONObject) parser.parse(reader);
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

    public static void main(String[] args) {
        Model model = new Model(4);
        model.loadData();

    }
}
