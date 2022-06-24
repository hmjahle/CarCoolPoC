package model;

import java.util.List;
import java.util.Map;
import java.lang.Short;

public class Model {
    // Stuff that goes in the model here
    private final List<Task> tasks;
    private final List<Shift> shifts;
    private final Map<Short, Shift> idsShifts;
    // denne har vi ikke helt enda: private final Map<Integer, ITravelTimeMatrix> travelTimeMatrix;

    public Model(List<Task> tasks, List<Shift> shifts, Map<Short, Shift> idsShifts) {
        this.tasks = tasks;
        this.shifts = shifts;
        this.idsShifts = idsShifts;
    }

}
