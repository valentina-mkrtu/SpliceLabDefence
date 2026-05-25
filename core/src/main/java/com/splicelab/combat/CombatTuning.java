package com.splicelab.combat;

public final class CombatTuning {
    private CombatTuning() {
    }

    public static final int MIN_DAMAGE = 1;
    public static final float MIN_ATTACK_INTERVAL_SECONDS = 0.45f;
    public static final float PROJECTILE_SPEED_PX_PER_SEC = 900f;
    // Extra delay after an enemy dies before the next can spawn.
    // Keep small so pacing feels responsive.
    public static final float ENEMY_SPAWN_DELAY_AFTER_DEATH_SECONDS = 1.2f;
    public static final float HIT_FLASH_DURATION_SECONDS = 0.08f;

    // Dynamic difficulty: scale enemies up when player has many fusions deployed.
    // Keeps early game readable (few fusions) while preventing late snowball.
    public static final float DYNAMIC_DIFFICULTY_PER_FUSION_EXTRA_HP = 0.30f;
    public static final float DYNAMIC_DIFFICULTY_PER_FUSION_EXTRA_ATK = 0.12f;
    public static final float DYNAMIC_DIFFICULTY_SPAWN_INTERVAL_MULT_PER_FUSION = 0.06f;
    public static final float DYNAMIC_DIFFICULTY_MIN_SPAWN_INTERVAL_MULT = 0.45f;
    public static final float DYNAMIC_DIFFICULTY_MAX_SPAWN_INTERVAL_MULT = 1.00f;

    // Spike scaling: additional enemy strength once player has 4+ fusions deployed.
    // 4 fusions => +20% HP/ATK. Every +2 fusions after 4 => +10% more.
    public static final int FUSION_SPIKE_START = 4;
    public static final float FUSION_SPIKE_BASE_MULT = 1.12f;
    public static final float FUSION_SPIKE_PER_2_FUSIONS_EXTRA = 0.08f;

    // Extra boss punch so final boss feels like a boss.
    public static final float BOSS_BASE_HP_MULT = 2.50f;
    public static final float BOSS_BASE_ATK_MULT = 2.00f;

    public static final float CONVEYOR_LOOP_SECONDS = 6.72f;
    public static final float CONVEYOR_STEP_INTERVAL_SECONDS = CONVEYOR_LOOP_SECONDS / 12f;
    public static final float CONVEYOR_MOVE_DURATION_SECONDS = 0.18f;
    // Checkpoint on the belt loop where fusions auto-attack.
    // Must match the UI marker index (LabGameView.ATTACK_MARKER_PATH_INDEX).
    public static final int ATTACK_ZONE_INDEX = 10;
}
