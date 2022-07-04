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

    public Visit(int id, Task task, boolean isVirtual) {
        this.id = id;
        this.task = task;
        this.isVirtual = true;
    }

    public Task getTask() {
        return this.task;
    }

    public int getId() {
        return this.id;
    }

    public boolean isVirtual() {
        return this.isVirtual;
    }

    public int getTransportType() {
        return this.transportType;
    }

    public int getTransportedBy(){
        return this.transportedBy;
    }

    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }

    public int getVisitDuration(){
        return 0;
    }
}
