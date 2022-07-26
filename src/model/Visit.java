package model;

import util.Constants.TransportMode;
import util.Constants.VisitType;


public class Visit {

    private int id;
    private Task task;
    private int visitType;
    private boolean isTransportTask;
    /**
     *  Transport type = 0 if driving was used to this task, and 1 if walking was used to this task
     *  Transported by refers to the id of the shift that transported you here (i.e., carpooling was used)
     */

    // 0 = drive, 1 = walk
    private int startTime;
    private int endTime;
    private Integer transportType;
    private Integer coCarPoolerShiftID; // Shift id to the person you are carpooling with

    private int travelTime;

    public Visit(int id, Task task, int visitType) {
        this.id = id;
        this.task = task;
        this.visitType = visitType;
    }

    public Visit(Visit visit) {
        this.id = visit.id;
        this.task = visit.task;
        this.visitType = visit.visitType;
        this.transportType = visit.transportType;
        this.coCarPoolerShiftID = visit.coCarPoolerShiftID;
        this.startTime = visit.startTime;
        this.endTime = visit.endTime;
        this.travelTime = visit.travelTime;
    }
    public void resetVisitWhenRemovedFromShift() {
        this.transportType = null;
        this.coCarPoolerShiftID = null;
    }

    // Getters

    public Task getTask() { return this.task; }

    public int getId() { return this.id; }

    public int getVisitType() { return this.visitType; }

    public boolean completesTask() {return this.visitType == VisitType.COMPLETE_TASK;}

    public int getTransportType() { return this.transportType; }

    public int getTaskStartTime(){ return this.task.getStartTime(); }

    public int getTaskEndTime(){ return this.task.getEndTime(); }

    public int getStartTime(){ return this.startTime;}

    public int getEndTime(){ return this.endTime;}

    public int getVisitDuration(){ if (this.isTransportTask){ return TransportMode.TRANSPORTTIME; } else { return this.getTask().getDuration(); } }
    
    public int getTravelTime() {
        return travelTime;
    }

    public Location getLocation(){ return this.task.getLocation();}

    public Integer getCoCarPoolerShiftID(){ 
        return this.coCarPoolerShiftID; 
    }


    // Setters
    public void setStartTime(int startTime){ this.startTime = startTime;}
    
    public void setEndTime(int endTime){ this.endTime = endTime;}

    public void setTravleTime(int travelTime){
        this.travelTime = travelTime;
    }

    public void setTransportType(Integer transportType) {
        this.transportType = transportType;
    }

    public void setCoCarPoolerShiftID(Integer coCarPoolerShiftID) {
        this.coCarPoolerShiftID = coCarPoolerShiftID;
    }
}
