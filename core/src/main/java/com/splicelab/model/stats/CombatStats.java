package com.splicelab.model.stats;

public record CombatStats(int maxHp, int atk, float attackIntervalSeconds, float specialChance, float variance) {
    public CombatStats {
        maxHp = Math.max(1, maxHp);
        atk = Math.max(0, atk);
        attackIntervalSeconds = Math.max(0.05f, attackIntervalSeconds);
        specialChance = clamp01(specialChance);
        variance = clamp(variance, 0f, 2f);
    }

    private static float clamp01(float v) {
        return clamp(v, 0f, 1f);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}

