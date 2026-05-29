package com.splicelab.services;

import java.util.Random;

public final class RandomService {
    private final Random random;

    public RandomService(Long seed) {
        this.random = seed == null ? new Random() : new Random(seed);
    }

    public int nextInt(int boundExclusive) {
        if (boundExclusive <= 0) return 0;
        return random.nextInt(boundExclusive);
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    /** Returns {@code true} with probability {@code p} (0..1). */
    public boolean chance(float p) {
        return random.nextFloat() < p;
    }

    /** Returns a random float in [min, max). */
    public float range(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}

