package model;
public class Task {

    // initierer med alle attributtene fra orginal ORP, så får vi heller se an hva vi fjerner underveis 
    private Long duration;
    private Long startTime;
    private Long endTime;
    private boolean isStrict;
    private boolean isTimeDependent;
    private int timeDependentOffsetInterval;
    private Long weight;
    private boolean requirePhysicalAppearance;
    private Location location;
    private int id;
    private int idFirstVirtual;
    private int idSecondVisit;
    private int idSecondVisitVirtual;
    private boolean prioritized;
    private int transportType;     // 0 - drive, 1 - walk
    private int transportedBy;

    public Task(int id, int numTasks ){
        this.id = id;
        this.idFirstVirtual = ( int) (id + 2 * numTasks);
        this.idSecondVisit = ( int) (id + numTasks);
        this.idSecondVisitVirtual = ( int) (id + 3 * numTasks);

    };
    
    // Setters 
    
    public void setTimeDependentOfSetInterval(int time) { this.timeDependentOffsetInterval = time;}

    public void setDuration(Long duration){ this.duration = duration;}

    public void setWeight(Long weight){ this.weight = weight;}

    public void setStartTime(Long startTime){ this.startTime = startTime;}
    
    public void setEndTime(Long endTime){ this.endTime = endTime;}

    public void setLocation(Location location){ this.location = location;}

    // Getters

    public int getId(){ return this.id; }

    public Long getStartTime(){ return this.startTime; }

    public Long getEndTime(){ return this.endTime; }

    public Long getTaskDuration(){ return this.duration; }

    public int getIsTimeDependentOfSetInterval(){ return this.timeDependentOffsetInterval; }

    public Long getWeight(){ return this.weight; }

    public boolean isStrict(){ return this.isStrict; }

    public boolean isTimeDependent(){ return this.isTimeDependent; }

    public boolean requirePhysicalAppearance(){ return this.requirePhysicalAppearance; }

    public boolean isPrioritized(){ return this.prioritized; }

    public Location getLocation(){ return this.location; }

    public int getTransportType(){ return this.transportType; }

    public int getTransportedBy(){ return this.transportedBy; }

    public  int getFirstVisitVirtualId() {
        return this.idFirstVirtual;
    }

    public  int getSecondVisitVirtualId() {
        return this.idSecondVisitVirtual;
    }

    public int getFirstVisitId() {
        return this.id;
    }

    public  int getSecondVisitId() {
        return this.idSecondVisit;
    }
}