package model;

import java.awt.Point; // bruker bare denne strukturen før vi får orden på noe annet

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
    private Point location;
    private short id;
    private short idFirstVirtual;
    private short idSecondVisit;
    private short idSecondVisitVirtual;
    private boolean prioritized;


    public Task(short id, short numTasks ){
        this.id = id;
        this.idFirstVirtual = (short) (id + 2 * numTasks);
        this.idSecondVisit = (short) (id + numTasks);
        this.idSecondVisitVirtual = (short) (id + 3 * numTasks);

    };

    public short getId(){
        return this.id;
    }

    public short getFirstVisitVirtualId() {
        return this.idFirstVirtual;
    }

    public short getSecondVisitVirtualId() {
        return this.idSecondVisitVirtual;
    }

    public short getFirstVisitId() {
        return this.id;
    }

    public short getSecondVisitId() {
        return this.idSecondVisits;
    }
}