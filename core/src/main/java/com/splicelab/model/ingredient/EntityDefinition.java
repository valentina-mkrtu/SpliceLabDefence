package com.splicelab.model.ingredient;

import com.splicelab.model.AttackElement;
import com.splicelab.model.EntityType;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.stats.CombatStats;
import com.splicelab.model.stats.SpecialAttackData;

public final class EntityDefinition extends IngredientDefinition {
    public final EntityType id;
    public final CombatStats baseStats;
    public final SpecialAttackData specialAttack;
    public final AttackElement defaultAttackElement;
    public final String displayNameKey;
    public final String shortDescriptionKey;
    public final String longDescriptionKey;
    public final String role;
    public final String projectileKey;
    public final String visualKey;
    public final String specialAttackId;
    public final boolean unlockDefault;

    public EntityDefinition(
            EntityType id,
            String displayName,
            CombatStats baseStats,
            SpecialAttackData specialAttack,
            AttackElement defaultAttackElement,
            String description,
            String displayNameKey,
            String shortDescriptionKey,
            String longDescriptionKey,
            String role,
            String projectileKey,
            String visualKey,
            String specialAttackId,
            boolean unlockDefault
    ) {
        super(IngredientKind.ENTITY, displayName, description);
        this.id = id;
        this.baseStats = baseStats;
        this.specialAttack = specialAttack;
        this.defaultAttackElement = defaultAttackElement;
        this.displayNameKey = displayNameKey == null ? "" : displayNameKey;
        this.shortDescriptionKey = shortDescriptionKey == null ? "" : shortDescriptionKey;
        this.longDescriptionKey = longDescriptionKey == null ? "" : longDescriptionKey;
        this.role = role == null ? "" : role;
        this.projectileKey = projectileKey == null ? "" : projectileKey;
        this.visualKey = visualKey == null ? "" : visualKey;
        this.specialAttackId = specialAttackId == null ? "" : specialAttackId;
        this.unlockDefault = unlockDefault;
    }
}
