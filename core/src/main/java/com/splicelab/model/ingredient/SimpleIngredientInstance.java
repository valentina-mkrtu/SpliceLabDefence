package com.splicelab.model.ingredient;

import com.splicelab.model.EntityType;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.ItemType;

public final class SimpleIngredientInstance implements IngredientInstance {
    private final String instanceId;
    private final IngredientKind kind;
    private final EntityType entityType;
    private final ItemType itemType;

    private SimpleIngredientInstance(String instanceId, IngredientKind kind, EntityType entityType, ItemType itemType) {
        this.instanceId = instanceId;
        this.kind = kind;
        this.entityType = entityType;
        this.itemType = itemType;
    }

    public static SimpleIngredientInstance ofEntity(String instanceId, EntityType type) {
        return new SimpleIngredientInstance(instanceId, IngredientKind.ENTITY, type, null);
    }

    public static SimpleIngredientInstance ofItem(String instanceId, ItemType type) {
        return new SimpleIngredientInstance(instanceId, IngredientKind.ITEM, null, type);
    }

    @Override
    public String instanceId() {
        return instanceId;
    }

    @Override
    public IngredientKind kind() {
        return kind;
    }

    public EntityType entityType() {
        return entityType;
    }

    public ItemType itemType() {
        return itemType;
    }
}

