package com.splicelab.app;

public final class GameConfig {
    public final int saveSchemaVersion;
    public final int maxConveyorSlotsPerSide;
    public final int maxTotalConveyorSlots;

    public final float tubeCooldownSeconds;
    public final int maxTubeCharges;
    public final int tubeMaxHp;

    public GameConfig(int saveSchemaVersion, int maxConveyorSlotsPerSide, float tubeCooldownSeconds, int maxTubeCharges, int tubeMaxHp) {
        this.saveSchemaVersion = saveSchemaVersion;
        this.maxConveyorSlotsPerSide = maxConveyorSlotsPerSide;
        this.maxTotalConveyorSlots = maxConveyorSlotsPerSide * 2;
        this.tubeCooldownSeconds = Math.max(0.1f, tubeCooldownSeconds);
        this.maxTubeCharges = Math.max(1, maxTubeCharges);
        this.tubeMaxHp = Math.max(1, tubeMaxHp);
    }

    public static GameConfig defaultConfig() {
        return new GameConfig(1, 3, 1.5f, 5, 300);
    }
}
