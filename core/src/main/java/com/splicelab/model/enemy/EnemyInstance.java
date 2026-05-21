package com.splicelab.model.enemy;

public final class EnemyInstance {
    public final String instanceId;
    public final EnemyType enemyType;
    public int hp;

    public EnemyInstance(String instanceId, EnemyType enemyType, int hp) {
        this.instanceId = instanceId;
        this.enemyType = enemyType;
        this.hp = Math.max(0, hp);
    }
}

