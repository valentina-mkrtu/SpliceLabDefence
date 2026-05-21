package com.splicelab.model.enemy;

public final class EnemyDefinition {
    public final EnemyType enemyType;
    public final String displayName;
    public final int maxHp;
    public final EnemyAttackDefinition attack;
    public final String projectileVisualKey;
    public final float rewardWeight;

    public EnemyDefinition(
            EnemyType enemyType,
            String displayName,
            int maxHp,
            EnemyAttackDefinition attack,
            String projectileVisualKey,
            float rewardWeight
    ) {
        this.enemyType = enemyType;
        this.displayName = displayName == null ? "" : displayName;
        this.maxHp = Math.max(1, maxHp);
        this.attack = attack;
        this.projectileVisualKey = projectileVisualKey == null ? "" : projectileVisualKey;
        this.rewardWeight = Math.max(0f, rewardWeight);
    }
}

