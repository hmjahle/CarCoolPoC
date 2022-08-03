package com.visma.of.cps.algorithm.heuristics;

public interface IStopSearchCriteria {
    boolean continueSearch();

    boolean deepDive();

    void startSearch();

    long expectedTemperatureUpdates();

    boolean shouldUpdateTemperature();

    /**
     * How close the stop criteria is to being finished.
     *
     * @return double value representing the percentage finished.
     */
    double percentageDone();

    /**
     * Runtime can be mean both time or iterations.
     * <p>
     * return long value representing either max time or iterations solver can run.
     */
    long getMaxRuntime();

    /**
     * Update stop criteria by assigning new max runtime, runtime to convergence and deep dive.
     * After using this method you need to call startSearch again.
     */
    void update(long maxRuntime, long runtimeToConverge, long deepDiveRuntime);
}
