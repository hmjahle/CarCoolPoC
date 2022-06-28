package algorithm.heuristics;

public class IterationsCriteria implements IStopSearchCriteria {

    private long maxIterations;
    private long iterationsToConverge;
    private long deepDiveIterations;
    private long iterations;
    private long expectedTemperatureUpdates;
    private long nextTemperatureUpdate;

    public IterationsCriteria(long maxIterations, long iterationsToConverge, long deepDiveIterations) {
        this.maxIterations = maxIterations;
        this.iterationsToConverge = iterationsToConverge;
        this.deepDiveIterations = deepDiveIterations;
    }

    @Override
    public boolean continueSearch() {
        iterations++;
        return iterations <= maxIterations;
    }

    @Override
    public boolean deepDive() {
        return iterations < deepDiveIterations;
    }

    @Override
    public void startSearch() {
        this.iterations = 0;
        this.nextTemperatureUpdate = 0;
        this.expectedTemperatureUpdates = Math.max(iterationsToConverge / 100, iterationsToConverge);
    }

    @Override
    public long expectedTemperatureUpdates() {
        return expectedTemperatureUpdates;
    }

    @Override
    public boolean shouldUpdateTemperature() {
        boolean update = iterations > nextTemperatureUpdate + deepDiveIterations;
        if (update)
            nextTemperatureUpdate += iterationsToConverge / expectedTemperatureUpdates;
        return update;
    }

    @Override
    public double percentageDone() {
        return ((double) Math.min(iterations, maxIterations)) / ((double) maxIterations);
    }

    @Override
    public long getMaxRuntime() {
        return maxIterations;
    }

    @Override
    public void update(long maxRuntime, long runtimeToConverge, long deepDiveRuntime) {
        this.maxIterations = maxRuntime;
        this.iterationsToConverge = runtimeToConverge;
        this.deepDiveIterations = deepDiveRuntime;
    }
}
