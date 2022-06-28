package algorithm.heuristics;

public class TimeCriteria implements IStopSearchCriteria {
    private long maxRuntime;
    private long timeToConverge;
    private long deepDiveTime;

    private long applyCoolingEveryMilliSeconds;
    private long startTime;
    private int temperatureUpdates;

    /**
     * maxRuntimeMilliseconds >= timeToConvergeMilliSeconds >= deepDiveTime.
     */
    public TimeCriteria(long maxRuntime, long timeToConverge, long deepDiveTime) {
        this.maxRuntime = maxRuntime;
        this.timeToConverge = timeToConverge;
        this.deepDiveTime = deepDiveTime;
    }

    @Override
    public void startSearch() {
        startTime = System.currentTimeMillis();
        temperatureUpdates = 0;
        long increments = expectedTemperatureUpdates();
        applyCoolingEveryMilliSeconds = (timeToConverge / increments) < 1 ? 1 : (timeToConverge / increments);
    }

    @Override
    public boolean deepDive() {
        return System.currentTimeMillis() - startTime < deepDiveTime;
    }

    @Override
    public boolean continueSearch() {
        return System.currentTimeMillis() - startTime < maxRuntime;
    }

    @Override
    public long expectedTemperatureUpdates() {
        return Math.max((int) (timeToConverge / 10.0), Math.min(10, (int) timeToConverge));
    }

    @Override
    public boolean shouldUpdateTemperature() {
        boolean shouldUpdate = System.currentTimeMillis() > applyCoolingEveryMilliSeconds * temperatureUpdates + (startTime + deepDiveTime);
        if (shouldUpdate)
            temperatureUpdates++;
        return shouldUpdate;
    }

    @Override
    public double percentageDone() {
        return ((double) (Math.min(maxRuntime, (System.currentTimeMillis()) - startTime)) / ((double) maxRuntime));
    }

    @Override
    public long getMaxRuntime() {
        return maxRuntime;
    }

    @Override
    public void update(long maxRuntime, long runtimeToConverge, long deepDiveRuntime) {
        this.maxRuntime = maxRuntime;
        this.timeToConverge = runtimeToConverge;
        this.deepDiveTime = deepDiveRuntime;
    }
}
