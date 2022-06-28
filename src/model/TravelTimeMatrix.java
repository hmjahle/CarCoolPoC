package model;

import java.util.Map;
import java.util.HashMap;

public class TravelTimeMatrix {
    
    private Map<Location, Map<Location, Double>> travelTimes;

    public TravelTimeMatrix(Map<Location, Map<Location, Double>> travelTimes){this.travelTimes = travelTimes; }
    
    public boolean connected(Location from, Location to){
        return this.travelTimes.containsKey(from) && travelTimes.get(from).containsKey(to);
    }

    public Map<Location, Map<Location, Double>> getTravelTimes() {
        return this.travelTimes;
    }

    // pluss masse greier her
}
