package com.splicelab.combat;

/**
 * All possible mid-level buff options shown to the player.
 * Each entry carries a display name and a short description.
 */
public enum MidLevelBuff {
    FUSION_ATK_UP("Fusion ATK +25%", "All deployed fusions deal 25% more damage this level."),
    TUBE_CHARGE("Free Tube Charge", "Tube charges are fully restored and cooldown reset."),
    FAST_COOLDOWN("Speedloader", "Tube cooldown is permanently halved for this level."),
    FUSION_HP_UP("Fusion HP +30%", "All currently deployed fusions regain 30% of max HP."),
    BELT_SPEED("Belt Rush", "Conveyor belt moves 40% faster, cycling fusions quicker."),
    ENEMY_SLOW("Enemy Slow", "The next enemy spawns with 30% reduced attack speed."),
    TIME_BONUS("Time Bonus +20s", "20 extra seconds added to the level timer."),
    ARMOR_STRIP("Armor Strip", "The current enemy loses all armor immediately.");

    public final String displayName;
    public final String description;

    MidLevelBuff(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
