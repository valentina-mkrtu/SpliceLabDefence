package com.splicelab.model.enemy;

/**
 * Live state of the currently-active enemy in combat.
 *
 * <p>New fields added for gameplay depth:</p>
 * <ul>
 *   <li>{@link #armor}  — absorbs the first N points of incoming damage (shield mechanic).</li>
 *   <li>{@link #inRage}  — set to {@code true} once HP drops below the rage threshold.</li>
 *   <li>{@link #chargingTell} — countdown before a telegraphed heavy attack fires.</li>
 *   <li>{@link #stunRemaining} — seconds the enemy's primary target cannot attack (anti-carry).</li>
 *   <li>{@link #stunnedSocketId} — which conveyor socket is currently stunned (-1 if none).</li>
 * </ul>
 */
public final class EnemyInstance {
    public final String instanceId;
    public final EnemyType enemyType;
    public int hp;
    public final int maxHp;

    // --- Armor / shield ----------------------------------------------------
    /** Remaining armor points.  Absorbs damage 1-for-1 before HP is touched. */
    public int armor;

    // --- Rage mechanic -----------------------------------------------------
    /** True once HP falls below the rage threshold (first time only). */
    public boolean inRage;

    // --- Tell / charge before big attack -----------------------------------
    /**
     * > 0 while the enemy is "winding up" a telegraphed heavy strike.
     * The attack fires when this reaches 0.
     */
    public float chargingTell;
    /** Damage amount stored during the tell so it fires correctly on expiry. */
    public int pendingTellDamage;

    // --- Anti-carry stun ---------------------------------------------------
    /** Seconds remaining on a stun applied to a fusion. */
    public float stunRemaining;
    /** Socket index of the stunned fusion, or -1 if none. */
    public int stunnedSocketId = -1;

    public EnemyInstance(String instanceId, EnemyType enemyType, int hp) {
        this.instanceId = instanceId;
        this.enemyType = enemyType;
        this.maxHp = Math.max(1, hp);
        this.hp = this.maxHp;
    }
}

