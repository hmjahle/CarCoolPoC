package model;
public class Task {

    // initierer med alle attributtene fra orginal ORP, så får vi heller se an hva vi fjerner underveis 
    private int duration;
    private int startTime;
    private int endTime;
    private boolean isStrict;
    private boolean isTimeDependent;
    private int timeDependentOffsetInterval;
    private int weight;
    private boolean requirePhysicalAppearance;
    private Location location;
    private short id;
    private boolean prioritized;
    // 0 - drive, 1 - walk
    private int transportType;
    private int transportedBy;

    public Task(){};
    
    // Setters 
    
    public void setTimeDependentOfSetInterval(int time) { this.timeDependentOffsetInterval = time;}
    
    // Getters

    public int getId(){ return this.id; }

    public int getStartTime(){ return this.startTime; }

    public int getEndTime(){ return this.endTime; }

    public int getTaskDuration(){ return this.duration; }

    public int getIsTimeDependentOfSetInterval(){ return this.timeDependentOffsetInterval; }

    public int getWeight(){ return this.weight; }

    public boolean isStrict(){ return this.isStrict; }

    public boolean isTimeDependent(){ return this.isTimeDependent; }

    public boolean requirePhysicalAppearance(){ return this.requirePhysicalAppearance; }

    public boolean isPrioritized(){ return this.prioritized; }

    public Location getLocation(){ return this.location; }

    public int getTransportType(){ return this.transportType; }

    public int getTransportedBy(){ return this.transportedBy; }
}