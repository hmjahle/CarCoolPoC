package model;

public class Visit {

    private int id;
    private Task task;
    private boolean isVirtual;

    /**
     *  Transport type = 0 if driving was used to this task, and 1 if walking was used to this task
     *  Transported by refers to the id of the shift that transported you here (i.e., carpooling was used)
     */

    // 0 = drive, 1 = walk
    private int transportType;
    private int transportedBy;

    public Visit(int id, Task task) {
        this.id = id;
        this.task = task;
        this.isVirtual = isVirtual;
    }

    public Task getTask() {
        return task;
    }

    public int getId() {
        return id;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public int getTransportType() {
        return transportType;
    }

    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }
}
