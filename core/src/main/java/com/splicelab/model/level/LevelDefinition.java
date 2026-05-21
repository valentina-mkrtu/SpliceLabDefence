package com.splicelab.model.level;

import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyType;

import java.util.List;

public final class LevelDefinition {
    public final int levelNumber;
    public final float durationSeconds;
    public final int tubeHp;
    public final int unlockedConveyorSlotsLeft;
    public final int unlockedConveyorSlotsRight;
    public final List<EntityType> availableEntities;
    public final List<ItemType> availableItems;
    public final List<EnemyType> enemyPool;
    public final float enemyHpMultiplier;
    public final float enemyAtkMultiplier;
    public final float spawnIntervalSeconds;
    public final LevelRewardDefinition rewards;
    public final String introMessage;
    public final String tutorialStepId;

    public LevelDefinition(
            int levelNumber,
            float durationSeconds,
            int tubeHp,
            int unlockedConveyorSlotsLeft,
            int unlockedConveyorSlotsRight,
            List<EntityType> availableEntities,
            List<ItemType> availableItems,
            List<EnemyType> enemyPool,
            float enemyHpMultiplier,
            float enemyAtkMultiplier,
            float spawnIntervalSeconds,
            LevelRewardDefinition rewards,
            String introMessage,
            String tutorialStepId
    ) {
        this.levelNumber = Math.max(1, levelNumber);
        this.durationSeconds = Math.max(1f, durationSeconds);
        this.tubeHp = Math.max(1, tubeHp);
        this.unlockedConveyorSlotsLeft = Math.max(0, unlockedConveyorSlotsLeft);
        this.unlockedConveyorSlotsRight = Math.max(0, unlockedConveyorSlotsRight);
        this.availableEntities = availableEntities == null ? List.of() : List.copyOf(availableEntities);
        this.availableItems = availableItems == null ? List.of() : List.copyOf(availableItems);
        this.enemyPool = enemyPool == null ? List.of() : List.copyOf(enemyPool);
        this.enemyHpMultiplier = Math.max(0.1f, enemyHpMultiplier);
        this.enemyAtkMultiplier = Math.max(0.1f, enemyAtkMultiplier);
        this.spawnIntervalSeconds = Math.max(0.2f, spawnIntervalSeconds);
        this.rewards = rewards == null ? new LevelRewardDefinition(0, 0) : rewards;
        this.introMessage = introMessage == null ? "" : introMessage;
        this.tutorialStepId = tutorialStepId;
    }
}

