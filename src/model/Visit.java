package model;

import util.Constants.TransportMode;

public class Visit {

    private int id;
    private Task task;
    private boolean isVirtual;
    private boolean isTransportTask;
    /**
     *  Transport type = 0 if driving was used to this task, and 1 if walking was used to this task
     *  Transported by refers to the id of the shift that transported you here (i.e., carpooling was used)
     */

    // 0 = drive, 1 = walk
    private int startTime;
    private int endTime;
    private int transportType;
    private int transportedBy;
    private int travelTime;

    public Visit(int id, Task task, boolean isVirtual) {
        this.id = id;
        this.task = task;
        this.isVirtual = true;
    }

    // Getters

    public Task getTask() { return this.task; }

    public int getId() { return this.id; }

    public boolean isVirtual() { return this.isVirtual; }

    public int getTransportType() { return this.transportType; }

    public int getTransportedBy(){ return this.transportedBy; }

    public void setTransportType(int transportType) {this.transportType = transportType; }

    public int getTaskStartTime(){ return this.task.getStartTime(); }

    public int getTaskEndTime(){ return this.task.getEndTime(); }

    public int getStartTime(){ return this.startTime;}

    public int getEndTime(){ return this.endTime;}

    public int getVisitDuration(){ if (this.isTransportTask){ return TransportMode.TRANSPORTTIME; } else { return this.getTask().getDuration(); } }
    
    public int getTravelTime() {
        return travelTime;
    }

    public Location getLocation(){ return this.task.getLocation();}

    // Setters
    public void setStartTime(int start_time){ this.startTime = start_time;}
    
    public void setEndTime(int end_time){ this.endTime = end_time;}

    public void setTravleTime(int travelTime){
        this.travelTime = travelTime;
    }
}
