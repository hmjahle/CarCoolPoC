package model;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.lang.Short;

public class Model {
    // Stuff that goes in the model here
    private final String filePath;
    private final Collection<Task> tasks;
    private final Map<Short, Shift> idsShifts;
    private final 
    
    // denne har vi ikke helt enda: private final Map<Integer, ITravelTimeMatrix> travelTimeMatrix;

    public Model(int modelInstance) {
        this.filePath = System.getProperty("user.dir") + "/resources/train_" + modelInstance + ".json"; 
    }

    public void loadData() {

        JSONParser parser = new JSONParser();

        try (
            FileReader reader = new FileReader(this.filePath)

        ) {

            JSONObject data = (JSONObject) parser.parse(reader);

            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
