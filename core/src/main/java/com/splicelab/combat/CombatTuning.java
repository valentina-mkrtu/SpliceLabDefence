package com.splicelab.combat;

public final class CombatTuning {
    private CombatTuning() {
    }

    public static final int MIN_DAMAGE = 1;
    public static final float MIN_ATTACK_INTERVAL_SECONDS = 0.45f;
    public static final float PROJECTILE_SPEED_PX_PER_SEC = 900f;
    public static final float ENEMY_SPAWN_DELAY_AFTER_DEATH_SECONDS = 0.35f;
    public static final float HIT_FLASH_DURATION_SECONDS = 0.08f;

    public static final float CONVEYOR_STEP_INTERVAL_SECONDS = 0.90f;
    public static final float CONVEYOR_MOVE_DURATION_SECONDS = 0.18f;
    public static final float CONVEYOR_LOOP_SECONDS = 6.72f;
    // Right-side checkpoint on the belt loop.
    public static final int ATTACK_ZONE_INDEX = 1;
}
