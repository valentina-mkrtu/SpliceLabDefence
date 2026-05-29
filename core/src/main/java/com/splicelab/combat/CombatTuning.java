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

    // Global enemy damage multiplier (vs tube and deployed fusions).
    public static final float ENEMY_DAMAGE_MULT = 1.30f;

    public static final float CONVEYOR_LOOP_SECONDS = 6.72f;
    public static final float CONVEYOR_STEP_INTERVAL_SECONDS = CONVEYOR_LOOP_SECONDS / 12f;
    public static final float CONVEYOR_MOVE_DURATION_SECONDS = 0.18f;
    // Checkpoint on the belt loop where fusions auto-attack.
    // Must match the UI marker index (LabGameView.ATTACK_MARKER_PATH_INDEX).
    public static final int ATTACK_ZONE_INDEX = 10;

    // -------------------------------------------------------------------------
    // Difficulty tier curve (getDifficultyTierFactor) — T-5.4
    // -------------------------------------------------------------------------
    /** Levels 1–TIER_GENTLE_CAP use the flat gentle factor. */
    public static final int TIER_GENTLE_CAP = 10;
    /** Base difficulty factor for gentle levels (1–10). */
    public static final float TIER_GENTLE_FACTOR = 0.35f;
    /** First level where the scaling ramp begins. */
    public static final int TIER_RAMP_START = 11;
    /** Number of levels per difficulty bump. */
    public static final int TIER_BUMP_INTERVAL = 5;
    /** Per-bump additive factor. */
    public static final float TIER_BUMP_FACTOR = 0.15f;
    /** Intra-band smooth-ramp contribution. */
    public static final float TIER_INTRA_BAND_FACTOR = 0.20f;
    /** Base factor at the start of the ramp (level 11). */
    public static final float TIER_RAMP_BASE = 0.55f;
    /** Hard minimum and maximum tier clamp. */
    public static final float TIER_MIN = 0.35f;
    public static final float TIER_MAX = 1.35f;

    /** HP fraction below which a fusion is considered low-health (for AI targeting). */
    public static final float FUSION_LOW_HEALTH_THRESHOLD = 0.35f;

    /** Probability the enemy targets the lowest-HP fusion (vs. random). */
    public static final float ENEMY_TARGET_LOW_HP_CHANCE = 0.70f;
}
