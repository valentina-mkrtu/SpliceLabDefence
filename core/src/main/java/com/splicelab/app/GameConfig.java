package com.splicelab.app;

public final class GameConfig {
    public final int saveSchemaVersion;
    public final int maxConveyorSlotsPerSide;
    public final int maxTotalConveyorSlots;

    public GameConfig(int saveSchemaVersion, int maxConveyorSlotsPerSide) {
        this.saveSchemaVersion = saveSchemaVersion;
        this.maxConveyorSlotsPerSide = maxConveyorSlotsPerSide;
        this.maxTotalConveyorSlots = maxConveyorSlotsPerSide * 2;
    }

    public static GameConfig defaultConfig() {
        return new GameConfig(1, 3);
    }
}

