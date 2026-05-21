package com.splicelab.model.ingredient;

import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;

public final class FusionDefinition {
    public final String id;
    public final EntityType entityType;
    public final ItemType itemType;
    public final String displayName;
    public final String projectileKey;
    public final String auraKey;
    public final String element;
    public final String specialBehavior;
    public final String tags;
    public final String displayNameKey;
    public final String shortDescriptionKey;
    public final String abilityNameKey;
    public final String abilityDescriptionKey;
    public final String silhouetteKey;

    public FusionDefinition(
            String id,
            EntityType entityType,
            ItemType itemType,
            String displayName,
            String projectileKey,
            String auraKey,
            String element,
            String specialBehavior,
            String tags,
            String displayNameKey,
            String shortDescriptionKey,
            String abilityNameKey,
            String abilityDescriptionKey,
            String silhouetteKey
    ) {
        this.id = id == null ? "" : id;
        this.entityType = entityType;
        this.itemType = itemType;
        this.displayName = displayName == null ? "" : displayName;
        this.projectileKey = projectileKey == null ? "" : projectileKey;
        this.auraKey = auraKey == null ? "" : auraKey;
        this.element = element == null ? "" : element;
        this.specialBehavior = specialBehavior == null ? "" : specialBehavior;
        this.tags = tags == null ? "" : tags;
        this.displayNameKey = displayNameKey == null ? "" : displayNameKey;
        this.shortDescriptionKey = shortDescriptionKey == null ? "" : shortDescriptionKey;
        this.abilityNameKey = abilityNameKey == null ? "" : abilityNameKey;
        this.abilityDescriptionKey = abilityDescriptionKey == null ? "" : abilityDescriptionKey;
        this.silhouetteKey = silhouetteKey == null ? "" : silhouetteKey;
    }
}
