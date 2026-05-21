package com.splicelab.data;

public final class BalanceRepository {
    public final float tubeSpawnCooldownSeconds;

    public BalanceRepository(float tubeSpawnCooldownSeconds) {
        this.tubeSpawnCooldownSeconds = Math.max(0f, tubeSpawnCooldownSeconds);
    }

    public static BalanceRepository createStarter() {
        return new BalanceRepository(0.6f);
    }
}

