package model;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

public class TravelTimeMatrix {
    
    private Map<Location, Map<Location, Double>> travelTimes;

    public TravelTimeMatrix(Map<Location, Map<Location, Double>> travelTimes){this.travelTimes = travelTimes; }
    
    public boolean connected(Location from, Location to){
        return this.travelTimes.containsKey(from) && travelTimes.get(from).containsKey(to);
    }

    public Map<Location, Map<Location, Double>> getTravelTimes() {
        return this.travelTimes;
    }

    public double getTravelTime(Location from, Location to) {
        return travelTimes.get(from).get(to);
    }

    public Collection<Location> getLocations() {
        return travelTimes.keySet();
    }

    // pluss masse greier her

}
