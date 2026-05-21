package com.splicelab.combat;

import com.splicelab.app.AppConstants;
import com.splicelab.model.level.LevelDefinition;
import com.splicelab.model.enemy.EnemyInstance;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;

public final class CombatState {
    public LevelDefinition level;
    public float remainingTimeSeconds;
    public int tubeHp;
    public float tubeCooldownRemaining;
    public int tubeCharges;
    public CombatResult result = CombatResult.PAUSED;

    public final IngredientInstance[][] grid = new IngredientInstance[AppConstants.GRID_COLS][AppConstants.GRID_ROWS];

    public final FusionInstance[] conveyorLeft;
    public final FusionInstance[] conveyorRight;

    public EnemyInstance activeEnemy;

    public float enemySpawnCooldownRemaining;

    public final float[] fusionAttackCooldownLeft;
    public final float[] fusionAttackCooldownRight;

    public float enemyAttackCooldownRemaining;

    public CombatState(int slotsPerSide) {
        conveyorLeft = new FusionInstance[slotsPerSide];
        conveyorRight = new FusionInstance[slotsPerSide];

        fusionAttackCooldownLeft = new float[slotsPerSide];
        fusionAttackCooldownRight = new float[slotsPerSide];
    }
}
