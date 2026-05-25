package com.splicelab.combat;

import com.splicelab.app.AppConstants;
import com.splicelab.model.enemy.EnemyInstance;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.level.LevelDefinition;


public final class CombatState {
    public int levelNumber;
    public LevelDefinition level;
    public float remainingTimeSeconds;
    public int tubeHp;
    public float tubeCooldownRemaining;
    public int tubeCharges;
    public int tubeMaxCharges;
    public CombatResult result = CombatResult.PAUSED;

    public final IngredientInstance[][] grid = new IngredientInstance[AppConstants.GRID_COLS][AppConstants.GRID_ROWS];

    // Tube spawn pity system (consecutive ITEM spawns).
    public int consecutiveItemSpawns;

    public final FusionInstance[] conveyorSockets;
    public final int[] conveyorSocketPathIndex;

    public float conveyorStepCooldownRemaining;
    // 0..1 phase representing belt movement progress.
    public float conveyorBeltPhase;

    public EnemyInstance activeEnemy;

    public int enemyWaveIndex;

    public float enemySpawnCooldownRemaining;
    public float enemyAttackCooldownRemaining;

    // Endless mode scaling.
    public boolean endlessMode;
    public float endlessElapsedSeconds;
    public int endlessScalingStepsApplied;
    public float endlessHpMultiplierBonus;
    public float endlessAtkMultiplierBonus;

    public final float[] fusionAttackCooldownSockets;

    // Boosts / powerups (activated from UI).
    public float timeFreezeSecondsRemaining;
    public float atkX2SecondsRemaining;
    public boolean removeItemArmed;

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

        timeFreezeSecondsRemaining = 0f;
        atkX2SecondsRemaining = 0f;
        removeItemArmed = false;
    }
}
