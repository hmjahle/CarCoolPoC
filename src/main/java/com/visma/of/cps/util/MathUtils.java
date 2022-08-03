package com.visma.of.cps.util;

import java.util.*;
import java.util.stream.Collectors;

public class MathUtils {
    
    private MathUtils() {
        // access static methods
    }

    /**
     * This function is performing the normalization so that element sum is equal to sumTo.
     *
     * @param collection collections of doubles that should be normalized
     * @param sumTo      the number it should normalize to. Typically, set to 1.0
     * @return list of normalized values
     */
    public static List<Double> normalizeElementSumToValue(Collection<Double> collection, double sumTo) {
        // divides all numbers on the total sum such that the sum will equal to 1.
        double sum = 0.0;
        for (Double element : collection) {
            sum += element;
        }
        sum = (sum == 0) ? 1 : sum;

        double divisor = sum / sumTo;
        return collection.stream()
                .map(x -> x / divisor)
                .collect(Collectors.toList());
    }


    /**
     * Checks if two intervals defined as (x1, x2) and (y1, y2) overlaps.
     */
    public static boolean doInclusiveIntervalsOverlap(int x1, int x2, int y1, int y2) {
        return Math.max(x1, y1) <= Math.min(x2, y2);
    }

    /**
     * Checks if two intervals defined as [x1, x2] and [y1, y2] overlaps.
     */
    public static boolean doExclusiveIntervalsOverlap(int x1, int x2, int y1, int y2) {
        return Math.max(x1, y1) < Math.min(x2, y2);
    }
}

