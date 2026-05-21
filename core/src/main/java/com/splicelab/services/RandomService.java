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
}

