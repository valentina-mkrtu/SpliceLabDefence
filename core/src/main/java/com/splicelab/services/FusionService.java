package com.splicelab.services;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.model.AttackElement;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.ingredient.EntityDefinition;
import com.splicelab.model.ingredient.FusionDefinition;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.ItemDefinition;
import com.splicelab.model.stats.CombatStats;

import java.util.Optional;

public final class FusionService {
    private final DefinitionRepository definitions;

    public FusionService(DefinitionRepository definitions) {
        this.definitions = definitions;
    }

    public boolean canFuse(EntityType entityType, ItemType itemType) {
        if (entityType == null || itemType == null) return false;
        return definitions.getFusion(entityType, itemType).isPresent();
    }

    public Optional<FusionInstance> createFusion(String instanceId, EntityType entityType, ItemType itemType) {
        if (instanceId == null || instanceId.isBlank() || entityType == null || itemType == null) {
            return Optional.empty();
        }

        Optional<EntityDefinition> e = definitions.getEntity(entityType);
        Optional<ItemDefinition> i = definitions.getItem(itemType);
        Optional<FusionDefinition> f = definitions.getFusion(entityType, itemType);
        if (e.isEmpty() || i.isEmpty() || f.isEmpty()) {
            Gdx.app.log(AppConstants.LOG_TAG, "Fusion rejected: missing definitions");
            return Optional.empty();
        }

        CombatStats base = e.get().baseStats;
        ItemDefinition item = i.get();

        int maxHp = Math.max(1, base.maxHp() + item.statModifiers.hp());
        int atk = Math.max(0, base.atk() + item.statModifiers.atk());
        float specialChance = clamp01(base.specialChance() + item.specialChanceBonus);
        float variance = clamp(base.variance() + item.varianceModifier, 0f, 2f);

        CombatStats finalStats = new CombatStats(maxHp, atk, base.attackIntervalSeconds(), specialChance, variance);
        AttackElement element = item.attackElement == null ? e.get().defaultAttackElement : item.attackElement;

        return Optional.of(new FusionInstance(
                instanceId,
                entityType,
                itemType,
                finalStats,
                element,
                f.get().displayName
        ));
    }

    private static float clamp01(float v) {
        return clamp(v, 0f, 1f);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}

