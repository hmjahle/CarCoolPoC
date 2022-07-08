package util;

public class Constants {

    public static class TransportMode {
        public static final int WALK = 1;
        public static final int DRIVE = 0;
        public static final int TRANSPORTTIME = 5;

    }

    public static class Penalty {
        public static final int STRICT_TIME_WINDOW_RELAXATION_PENALTY_DEFAULT = 10;
        

    }

    public static final double TRAVEL_TIME_WEIGTH = 0.5;
    public static final double TIME_WINDOW_WEIGHT = 0.5;
    public static final double OVERTIME_WEIGHT = 0.3;

    public static final int MAX_WALK_TIME = 10;
    public static final int SOLVER_RUNTIME = 5*60;

}
