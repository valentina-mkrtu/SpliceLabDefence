package com.splicelab.data;

public final class UnlockRepository {
    private final int maxSlotsPerSide;

    public UnlockRepository(int maxSlotsPerSide) {
        this.maxSlotsPerSide = Math.max(1, maxSlotsPerSide);
    }

    public int getMaxSlotsPerSide() {
        return maxSlotsPerSide;
    }

    public static UnlockRepository createStarter(int maxSlotsPerSide) {
        return new UnlockRepository(maxSlotsPerSide);
    }
}

