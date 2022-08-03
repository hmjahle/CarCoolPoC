package com.visma.of.cps.model;

import java.util.Collection;
import java.util.Map;

public class TravelTimeMatrix {
    
    private Map<Location, Map<Location, Integer>> travelTimes;

    public TravelTimeMatrix(Map<Location, Map<Location, Integer>> travelTimes){this.travelTimes = travelTimes; }
    
    public boolean connected(Location from, Location to){
        return this.travelTimes.containsKey(from) && travelTimes.get(from).containsKey(to);
    }

    public Map<Location, Map<Location, Integer>> getTravelTimes() {
        return this.travelTimes;
    }

    public int getTravelTime(Location from, Location to) {
        return travelTimes.get(from).get(to);
    }

    public Collection<Location> getLocations() {
        return travelTimes.keySet();
    }

    // pluss masse greier her

}
