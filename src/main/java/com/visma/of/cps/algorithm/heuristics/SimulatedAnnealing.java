package com.visma.of.cps.algorithm.heuristics;

import java.util.Random;

/**
 * The simulated annealing can be used with runtime or iterations as the stop criteria. Therefore only one of them can be set.
 * <p>
 * The search criteria use a Simulated Annealing approach to accept a new solution.
 * Hence, in addition to accept any improving solution, the acceptance criteria calculates the probability of
 * accepting a solution using the following formula:
 * e^(-deltaValue/temperature).
 * <p>
 * Which means the lower the delta value is the higher probability there is to accept the solution.
 * At the same time, a higher temperature translates to higher probability of accepting the solution.
 * This translates to a temperature equal to the delta value will be accepted at ~36%.
 * <p>
 * The temperature decrease by the cooling factor throughout the search, therefore the probability of accepting
 * a deteriorating solution in the start of the search is higher than later in the search.
 * First the strategy is to find an starting solution value that can be used to initialize the temperature.
 * Therefore the first (deepDiveTime / iterations) of the (max runtime/iterations) will be used for a deep dive, i.e.,
 * only strictly improving solutions will be accepted.
 * Thereafter the temperature and cooling values are initialized and used for the remainder of the search.
 */
public class SimulatedAnnealing {
    private final Random random;
    private final double percentWorseSolutionThatCanBeAcceptedAtProbability;

    private long startTime;
    private double temperature;
    private double cooling;
    private IStopSearchCriteria stopSearchCriteria;


    public SimulatedAnnealing(double percentWorseSolutionThatCanBeAcceptedAtProbability) {
        this.percentWorseSolutionThatCanBeAcceptedAtProbability = percentWorseSolutionThatCanBeAcceptedAtProbability;
        this.random = new Random();
    }

    /**
     * @param maxRuntimeMilliSeconds
     * @param timeToConvergeMilliSeconds
     * @param deepDiveTime
     */
    public void setTimeStopCriteria(long maxRuntimeMilliSeconds, long timeToConvergeMilliSeconds, long deepDiveTime) {
        stopSearchCriteria = new TimeCriteria(maxRuntimeMilliSeconds, timeToConvergeMilliSeconds, deepDiveTime);
    }

    public void setIterationsStopCriteria(long maxIterations, long iterationsToConverge, long deepDiveIterations) {
        stopSearchCriteria = new IterationsCriteria(maxIterations, iterationsToConverge, deepDiveIterations);
    }

    /**
     * Initialize the search, runtime.
     */
    public void startSearch() {
        temperature = Integer.MAX_VALUE;
        startTime = System.currentTimeMillis();
        if (stopSearchCriteria == null)
            throw new NullPointerException("A stop criteria must be set.");
        stopSearchCriteria.startSearch();
    }


    /**
     * Initialize the temperature and cooling parameters.
     *
     * @param initializationSolutionValue Value used to calculate the temperature. The temperature will be set
     *                                    such that there is a 50% likelihood that a delta value with a delta of
     *                                    percentWorseSolutionThatCanBeAcceptedAtProbability * initializationSolutionValue
     *                                    will be accepted.
     */
    private void initializeTemperature(double initializationSolutionValue) {
        temperature = -(initializationSolutionValue * (percentWorseSolutionThatCanBeAcceptedAtProbability)) / Math.log(.5);
        temperature = Math.max(temperature, 1E-6);
        double endTemp = initializationSolutionValue * 0.0001;
        long increments = stopSearchCriteria.expectedTemperatureUpdates();
        cooling = Math.pow((endTemp / temperature), (1.0 / increments));
    }

    /**
     * Should the current solution be accepted according to the stopping criteria.
     *
     * @param bestKnownFitness Solution value of the best known solution.
     *                         Used to initialize temperature and cooling parameters.
     * @param deltaFitness     Change in solution value with respect to the current solution.
     * @return Whether the solution should be accepted.
     */
    public boolean acceptSolution(double bestKnownFitness, double deltaFitness) {
        if (stopSearchCriteria.deepDive())
            return deltaFitness <= 0;

        updateTemperature(bestKnownFitness);
        return acceptDeltaFitness(deltaFitness);
    }

    /**
     * Evaluate whether the search should continue, according to the max runtime criteria.
     * If none of them are set, this function will always return true.
     *
     * @return Whether the search should continue.
     */
    public boolean continueSearch() {
        return stopSearchCriteria.continueSearch();
    }

    /**
     * Initialize or reduce the temperature.
     *
     * @param initializationFitness
     */
    private void updateTemperature(double initializationFitness) {
        if (temperature == Integer.MAX_VALUE)
            initializeTemperature(initializationFitness);
        while (stopSearchCriteria.shouldUpdateTemperature()) {
            temperature *= cooling;
        }
    }

    /**
     * Whether to accept a solution with the delta fitness, it will always be accepted when it is strictly improving.
     *
     * @param deltaFitness Value to be considered to be accepted.
     * @return
     */
    private boolean acceptDeltaFitness(double deltaFitness) {
        double acceptThreshold = Math.exp(-(deltaFitness) / Math.max(temperature, 1E-6)) - 1E-6;
        return deltaFitness <= 0 || random.nextDouble() < acceptThreshold;
    }

    public long getCurrentRuntime() {
        return startTime == 0 ? 0 : System.currentTimeMillis() - startTime;
    }

    /**
     * How close the stop criteria is to being finished.
     *
     * @return double value representing the percentage finished.
     */
    public double percentageDone() {
        return stopSearchCriteria.percentageDone();
    }

    public IStopSearchCriteria getStopSearchCriteria() {
        return stopSearchCriteria;
    }
}
