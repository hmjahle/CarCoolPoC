package com.visma.of.cps.util;


import java.util.List;
import java.util.Random;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * Utility class containing common functions for randomness.
 */
public class RandomUtils {

    private static final double MAX_NOISE_ON_OBJECTIVE_VALUE = .05;

    private RandomUtils() {

    }

    public static double objectiveNoise(Random random) {
        return (1 - MAX_NOISE_ON_OBJECTIVE_VALUE / 2) + MAX_NOISE_ON_OBJECTIVE_VALUE * random.nextDouble();
    }

    /**
     * Returns a next random element from a given list.
     *
     * @param elements list of elements
     * @param <T>      container type
     * @return random element or null if the list is empty
     */
    public static <T> T next(List<T> elements) {
        return !elements.isEmpty() ? elements.get(current().nextInt(elements.size())) : null;
    }

    /**
     * Returns a next random element from a given list of elements. At most
     * the first maxSize elements are being used.
     *
     * @param elements list of elements
     * @param maxSize  the number of elements to consider.
     * @param <T>      container type
     * @return random element or null if the list is empty
     */
    public static <T> T next(List<T> elements, int maxSize) {
        int size = Math.min(maxSize, elements.size());
        return !elements.isEmpty() ? elements.get(current().nextInt(size)) : null;
    }


}
