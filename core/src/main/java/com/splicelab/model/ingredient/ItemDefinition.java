package com.splicelab.model.ingredient;

import com.splicelab.model.AttackElement;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.ItemType;
import com.splicelab.model.stats.StatBlock;

public final class ItemDefinition extends IngredientDefinition {
    public final ItemType id;
    public final StatBlock statModifiers;
    public final float specialChanceBonus;
    public final float varianceModifier;
    public final AttackElement attackElement;
    public final float attackIntervalModifierSeconds;
    public final String displayNameKey;
    public final String shortDescriptionKey;
    public final String longDescriptionKey;
    public final String role;
    public final String projectileModifierKey;
    public final String visualKey;
    public final boolean unlockDefault;

    public ItemDefinition(
            ItemType id,
            String displayName,
            StatBlock statModifiers,
            float specialChanceBonus,
            float varianceModifier,
            AttackElement attackElement,
            float attackIntervalModifierSeconds,
            String description,
            String displayNameKey,
            String shortDescriptionKey,
            String longDescriptionKey,
            String role,
            String projectileModifierKey,
            String visualKey,
            boolean unlockDefault
    ) {
        super(IngredientKind.ITEM, displayName, description);
        this.id = id;
        this.statModifiers = statModifiers;
        this.specialChanceBonus = specialChanceBonus;
        this.varianceModifier = varianceModifier;
        this.attackElement = attackElement;
        this.attackIntervalModifierSeconds = attackIntervalModifierSeconds;
        this.displayNameKey = displayNameKey == null ? "" : displayNameKey;
        this.shortDescriptionKey = shortDescriptionKey == null ? "" : shortDescriptionKey;
        this.longDescriptionKey = longDescriptionKey == null ? "" : longDescriptionKey;
        this.role = role == null ? "" : role;
        this.projectileModifierKey = projectileModifierKey == null ? "" : projectileModifierKey;
        this.visualKey = visualKey == null ? "" : visualKey;
        this.unlockDefault = unlockDefault;
    }
}
