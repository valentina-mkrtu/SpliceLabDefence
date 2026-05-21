package com.splicelab.model.ingredient;

import com.splicelab.model.AttackElement;
import com.splicelab.model.EntityType;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.ItemType;
import com.splicelab.model.stats.CombatStats;

public final class FusionInstance implements IngredientInstance {
    private final String instanceId;
    public final EntityType entityType;
    public final ItemType itemType;
    public final CombatStats stats;
    public final AttackElement attackElement;
    public final String displayName;

    public FusionInstance(
            String instanceId,
            EntityType entityType,
            ItemType itemType,
            CombatStats stats,
            AttackElement attackElement,
            String displayName
    ) {
        this.instanceId = instanceId;
        this.entityType = entityType;
        this.itemType = itemType;
        this.stats = stats;
        this.attackElement = attackElement;
        this.displayName = displayName == null ? "" : displayName;
    }

    @Override
    public String instanceId() {
        return instanceId;
    }

    @Override
    public IngredientKind kind() {
        return IngredientKind.FUSION;
    }
}

