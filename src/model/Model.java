package model;

import java.util.List;
import java.util.Map;
import java.lang.Short;

public class Model {
    // Stuff that goes in the model here
    private final int instance;
    private final List<Task> tasks;
    private final List<Shift> shifts;
    private final Map<Short, Shift> idsShifts;
    // denne har vi ikke helt enda: private final Map<Integer, ITravelTimeMatrix> travelTimeMatrix;

    public Model(int modelInstance) {
        this.instance = modelInstance;
    }

    public void loadData() {
        JSONArray data = new JSONArray();
    }

}
