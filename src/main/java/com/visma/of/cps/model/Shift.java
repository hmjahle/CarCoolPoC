package com.visma.of.cps.model;

import java.awt.Point; // bruker bare denne strukturen før vi får orden på noe annet

public class Shift {

    private final int id;
    private Point startLocation;
    private Point endLocation;
    private boolean motorized;
    private int capacity;
    private int startTime;
    private int endTime;
    private boolean carpoolAble;

    public Shift(int id, boolean carpoolAble, boolean motorised){
        this.id = id;
        this.carpoolAble = carpoolAble;
        this.motorized = motorised;
    }

    // Setters 
    public void setCapacity(Integer capacity){
        this.capacity = capacity;
    }

    // Getters
    public boolean isMotorized() {
        return motorized;
    }

    public int getId(){ return this.id; }

    public Point getStartLocation(){ return this.startLocation; }

    public Point getEndLocation(){ return this.endLocation; } 

    public int getCapacity(){ return this.capacity; }

    public int getStartTime(){ return this.startTime; }

    public int getTimeWindowEnd(){ return this.endTime;}

    public int getShiftDuration(){ return (this.endTime - this.startTime); }

    public boolean getCarpoolAble(){ return this.carpoolAble;}

}