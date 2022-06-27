package model;

import java.util.Map;
import java.util.HashMap;

public class TravelTimeMatrix {
    
    private Map<Location, Map<Location, Integer>> travelTimes;

    public TravelTimeMatrix(){this.travelTimes = new HashMap<>();}
    
    public boolean connected(Location from, Location to){
        return this.travelTimes.containsKey(from) && travelTimes.get(from).containsKey(to);
    }

    // pluss masse greier her
}
