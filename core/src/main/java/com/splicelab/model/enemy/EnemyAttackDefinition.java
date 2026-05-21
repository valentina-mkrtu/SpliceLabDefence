package com.splicelab.model.enemy;

public record EnemyAttackDefinition(int damage, float intervalSeconds) {
    public EnemyAttackDefinition {
        damage = Math.max(1, damage);
        intervalSeconds = Math.max(0.2f, intervalSeconds);
    }
}

