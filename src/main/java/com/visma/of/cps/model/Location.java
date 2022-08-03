package com.visma.of.cps.model;

public class Location {

    private int id;
    private double xCoord;
    private double yCoord;

    public Location(Integer id, double xCoord, double yCoord){ this.id = id; this.xCoord = xCoord; this.yCoord = yCoord;}

    public int getId(){ return this.id; }

    public double getXCoord() {return this.xCoord;}

    public double getYCoord(){ return this.yCoord;}
    
}