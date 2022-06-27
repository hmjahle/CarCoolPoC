package model;

public class Visit {

    private short id;
    private Task task;

    /**
     *  Transport type = 0 if driving was used to this task, and 1 if walking was used to this task
     *  Transported by refers to the id of the shift that transported you here (i.e., carpooling was used)
     */

    // 0 = drive, 1 = walk
    private int transportType;
    private int transportedBy;

    public Visit(short id, Task task) {
        this.id = id;
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public short getId() {
        return id;
    }
}
