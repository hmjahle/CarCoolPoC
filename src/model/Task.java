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
    private boolean prioritized;

    public Task(){};
}