package com.splicelab.model.level;

import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyType;

import java.util.List;

public final class LevelDefinition {
    public final int levelNumber;
    public final float durationSeconds;
    public final int tubeHp;
    public final float tubeCooldownSeconds;
    public final int maxTubeCharges;
    public final int unlockedConveyorSlotsLeft;
    public final int unlockedConveyorSlotsRight;
    public final List<EntityType> availableEntities;
    public final List<ItemType> availableItems;
    public final List<EnemySpawnEntry> enemyPool;
    public final java.util.List<com.splicelab.model.enemy.EnemyType> enemyWave;
    public final float enemyHpMultiplier;
    public final float enemyAtkMultiplier;
    public final float spawnIntervalSeconds;

    // Tube bag: exact 8-pick composition per level.
    public final java.util.List<com.splicelab.services.TubeSpawnService.SpawnChoice> tubeSpawnBag8;
    public final LevelRewardDefinition rewards;
    public final String introMessage;
    public final String tutorialStepId;

    public LevelDefinition(
            int levelNumber,
            float durationSeconds,
            int tubeHp,
            float tubeCooldownSeconds,
            int maxTubeCharges,
            int unlockedConveyorSlotsLeft,
            int unlockedConveyorSlotsRight,
            List<EntityType> availableEntities,
            List<ItemType> availableItems,
            List<EnemySpawnEntry> enemyPool,
            java.util.List<com.splicelab.model.enemy.EnemyType> enemyWave,
            float enemyHpMultiplier,
            float enemyAtkMultiplier,
            float spawnIntervalSeconds,
            java.util.List<com.splicelab.services.TubeSpawnService.SpawnChoice> tubeSpawnBag8,
            LevelRewardDefinition rewards,
            String introMessage,
            String tutorialStepId
    ) {
        this.levelNumber = Math.max(1, levelNumber);
        this.durationSeconds = Math.max(1f, durationSeconds);
        this.tubeHp = Math.max(1, tubeHp);
        this.tubeCooldownSeconds = Math.max(0.1f, tubeCooldownSeconds);
        this.maxTubeCharges = Math.max(1, maxTubeCharges);
        this.unlockedConveyorSlotsLeft = Math.max(0, unlockedConveyorSlotsLeft);
        this.unlockedConveyorSlotsRight = Math.max(0, unlockedConveyorSlotsRight);
        this.availableEntities = availableEntities == null ? List.of() : List.copyOf(availableEntities);
        this.availableItems = availableItems == null ? List.of() : List.copyOf(availableItems);
        this.enemyPool = enemyPool == null ? List.of() : List.copyOf(enemyPool);
        this.enemyWave = enemyWave == null ? java.util.List.of() : java.util.List.copyOf(enemyWave);
        this.enemyHpMultiplier = Math.max(0.1f, enemyHpMultiplier);
        this.enemyAtkMultiplier = Math.max(0.1f, enemyAtkMultiplier);
        this.spawnIntervalSeconds = Math.max(0.2f, spawnIntervalSeconds);
        this.tubeSpawnBag8 = tubeSpawnBag8 == null ? java.util.List.of() : java.util.List.copyOf(tubeSpawnBag8);
        this.rewards = rewards == null ? LevelRewardDefinition.of(0, 0) : rewards;
        this.introMessage = introMessage == null ? "" : introMessage;
        this.tutorialStepId = tutorialStepId;
    }

    public record EnemySpawnEntry(EnemyType enemyType, float weight) {
        public EnemySpawnEntry {
            weight = Math.max(0f, weight);
        }
    }
}
