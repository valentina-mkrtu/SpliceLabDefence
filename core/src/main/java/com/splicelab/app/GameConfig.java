package com.splicelab.app;

public final class GameConfig {
    public final int saveSchemaVersion;
    public final int maxConveyorSlotsPerSide;
    public final int maxTotalConveyorSlots;

    public final float tubeCooldownSeconds;
    public final int maxTubeCharges;
    public final int tubeMaxHp;

    public final int gridCols;
    public final int gridRows;
    public final int gridTotalSlots;

    public final int tubeSpawnPerTap;
    public final float spawnEntityWeight;
    public final float spawnItemWeight;
    public final int pityGuaranteeEntityEveryXItemSpawns;

    public final float defeatCoinsMultiplier;
    public final float defeatDnaMultiplier;

    public GameConfig(
            int saveSchemaVersion,
            int maxConveyorSlotsPerSide,
            float tubeCooldownSeconds,
            int maxTubeCharges,
            int tubeMaxHp,
            int gridCols,
            int gridRows,
            int gridTotalSlots,
            int tubeSpawnPerTap,
            float spawnEntityWeight,
            float spawnItemWeight,
            int pityGuaranteeEntityEveryXItemSpawns
            ,
            float defeatCoinsMultiplier,
            float defeatDnaMultiplier
    ) {
        this.saveSchemaVersion = saveSchemaVersion;
        this.maxConveyorSlotsPerSide = maxConveyorSlotsPerSide;
        this.maxTotalConveyorSlots = maxConveyorSlotsPerSide * 2;
        this.tubeCooldownSeconds = Math.max(0.1f, tubeCooldownSeconds);
        this.maxTubeCharges = Math.max(1, maxTubeCharges);
        this.tubeMaxHp = Math.max(1, tubeMaxHp);

        this.gridCols = Math.max(1, gridCols);
        this.gridRows = Math.max(1, gridRows);
        this.gridTotalSlots = Math.max(1, gridTotalSlots);

        this.tubeSpawnPerTap = Math.max(1, tubeSpawnPerTap);
        this.spawnEntityWeight = Math.max(0f, spawnEntityWeight);
        this.spawnItemWeight = Math.max(0f, spawnItemWeight);
        this.pityGuaranteeEntityEveryXItemSpawns = Math.max(0, pityGuaranteeEntityEveryXItemSpawns);

        this.defeatCoinsMultiplier = Math.max(0f, defeatCoinsMultiplier);
        this.defeatDnaMultiplier = Math.max(0f, defeatDnaMultiplier);
    }

    public static GameConfig defaultConfig() {
        return new GameConfig(
                1,
                3,
                1.5f,
                5,
                300,
                3,
                4,
                12,
                1,
                0.5f,
                0.5f,
                0,
                0.25f,
                0.0f
        );
    }
}
