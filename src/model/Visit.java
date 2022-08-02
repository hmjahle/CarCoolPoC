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
    private Integer timeDependentOffsetInterval; //NB!!! når vi setter offset må det være lovlig innenfor time intervallet for tasken
    private Integer startTime;
    private Integer endTime;
    private Integer transportType;
    private Integer coCarPoolerShiftID; // Shift id to the person you are carpooling with
    private boolean isSynced = false;

    private int travelTime;

    public Visit(int id, Task task, int visitType) {
        this.id = id;
        this.task = task;
        this.visitType = visitType;
        if (this.id == this.task.getId()) {
            // We can only initialize time interval if this
            // is a task completing visit
            this.startTime = this.task.getStartTime();
            this.endTime = this.task.getEndTime();
        }
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
        this.timeDependentOffsetInterval = visit.timeDependentOffsetInterval;

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
    public boolean isJoinMotorized() {return this.visitType == VisitType.JOIN_MOTORIZED;}
    public boolean isDropOff() {return this.visitType == VisitType.DROP_OF;}
    public boolean isPickUp() {return this.visitType == VisitType.PICK_UP;}


    public int getTransportType() { return this.transportType; }

    public int getTaskStartTime(){ return this.task.getStartTime(); }

    public int getTaskEndTime(){ return this.task.getEndTime(); }

    public Integer getStartTime(){ return this.startTime;}

    public Integer getEndTime(){ return this.endTime;}

    public int getVisitDuration(){ if (this.isTransportTask){ return TransportMode.TRANSPORTTIME; } else { return this.getTask().getDuration(); } }
    
    public int getTravelTime() {
        return travelTime;
    }

    public boolean isSynced(){ return this.isSynced;}

    public Location getLocation(){ return this.task.getLocation();}

    public Integer getCoCarPoolerShiftID(){ 
        return this.coCarPoolerShiftID; 
    }
    public Integer getTimeDependentOffsetInterval(){
        return this.timeDependentOffsetInterval;
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

    public void setIsSynced(boolean isSynced){
        this.isSynced = isSynced;
    }

    public void removeCarPooling(){
        setIsSynced(false);
        setCoCarPoolerShiftID(null);
        setTimeDependentOffsetInterval(null);
    }

    public void setCarpooling(int coCarPoolerShiftID, int syncedStartTime, int timeDependentOffsetInterval){
        setIsSynced(true);
        setCoCarPoolerShiftID(coCarPoolerShiftID);
        setTimeDependentOffsetInterval(timeDependentOffsetInterval);
        setStartTime(syncedStartTime);
    }
    
    public void setTimeDependentOffsetInterval(Integer timeDependentOffsetInterval){
        this.timeDependentOffsetInterval = timeDependentOffsetInterval;
    }
}
