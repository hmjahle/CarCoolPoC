package com.visma.of.cps.algorithm.operators;

import java.util.Random;

/**
 * Mixin like class for operators.
 */
public abstract class OperatorAbstract {

    private static final double MAX_NOISE_ON_OBJECTIVE_VALUE = .05;

    protected Random random;

    protected OperatorAbstract(Random random) {
        this.random = random;
    }

    public static double noise(Random random) {
        return (1 - MAX_NOISE_ON_OBJECTIVE_VALUE / 2) + MAX_NOISE_ON_OBJECTIVE_VALUE * random.nextDouble();
    }


}
