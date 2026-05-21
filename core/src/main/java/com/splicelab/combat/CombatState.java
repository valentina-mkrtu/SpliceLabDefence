package com.splicelab.combat;

import com.splicelab.app.AppConstants;
import com.splicelab.model.enemy.EnemyInstance;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.level.LevelDefinition;

public final class CombatState {
    public LevelDefinition level;
    public float remainingTimeSeconds;
    public int tubeHp;
    public float tubeCooldownRemaining;
    public int tubeCharges;
    public int tubeMaxCharges;
    public CombatResult result = CombatResult.PAUSED;

    public final IngredientInstance[][] grid = new IngredientInstance[AppConstants.GRID_COLS][AppConstants.GRID_ROWS];

    public final FusionInstance[] conveyorSockets;
    public final int[] conveyorSocketPathIndex;

    public float conveyorStepCooldownRemaining;

    public EnemyInstance activeEnemy;

    public float enemySpawnCooldownRemaining;
    public float enemyAttackCooldownRemaining;

    public final float[] fusionAttackCooldownSockets;

    public CombatState(int slotsPerSide) {
        // Real 12-socket conveyor.
        int sockets = 12;
        conveyorSockets = new FusionInstance[sockets];
        conveyorSocketPathIndex = new int[sockets];
        fusionAttackCooldownSockets = new float[sockets];
        for (int i = 0; i < sockets; i++) {
            conveyorSocketPathIndex[i] = i;
            fusionAttackCooldownSockets[i] = 0f;
        }
    }
}
