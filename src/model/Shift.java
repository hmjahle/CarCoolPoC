package model;

import java.awt.Point; // bruker bare denne strukturen før vi får orden på noe annet

public class Shift {

    private final int id;
    private Point startLocation;
    private Point endLocation;
    private int motorized;
    private int capacity;
    private int startTime;
    private int endTime;

    public Shift(int id){
        this.id = id;
    }

    // Setters 
    public void setCapacity(Integer capacity){
        this.capacity = capacity;
    }

    // Getters
    public int getShiftId(){ return this.id; }

    public Point getStartLocation(){ return this.startLocation; }

    public Point getEndLocation(){ return this.endLocation; } 

    public int getTransportMode(){ return this.transportMode; }

    public int getCapacity(){ return this.capacity; }

    public int getStartTime(){ return this.startTime; }

    public int getEndTime(){ return this.endTime;}

    public int getShiftDuration(){ return (this.endTime - this.startTime); }

}