package com.splicelab.ui;

import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;

public final class IngredientArt {
    private IngredientArt() {
    }

    public static String entityIconPath(EntityType type) {
        if (type == null) return null;
        return switch (type) {
            case SLIME -> "art/entities/slime.png";
            case MECH -> "art/entities/mech.png";
            case FUNGUS -> "art/entities/fungy.png";
        };
    }

    public static String itemIconPath(ItemType type) {
        if (type == null) return null;
        return switch (type) {
            case BATTERY -> "art/items/battery.png";
            case TOXIC_WASTE -> "art/items/toxicwaste.png";
            case RADIOACTIVE_GOO -> "art/items/radioactivegoo.png";
            case CRYOGEL -> "art/items/criogel.png";
            case CRYSTAL_SHARD -> "art/items/crystalshard.png";
            case NANOBOTS -> "art/items/nanobots.png";
        };
    }

    public static String fusionIconPath(EntityType entityType, ItemType itemType) {
        if (entityType == null || itemType == null) return null;
        return switch (entityType) {
            case SLIME -> switch (itemType) {
                case BATTERY -> "art/fusions/electroslime.png";
                case TOXIC_WASTE -> "art/fusions/toxicslime.png";
                case RADIOACTIVE_GOO -> "art/fusions/radioactiveslime.png";
                case CRYOGEL -> "art/fusions/crioslime.png";
                case CRYSTAL_SHARD -> "art/fusions/crystalslime.png";
                case NANOBOTS -> "art/fusions/nanoslime.png";
            };
            case MECH -> switch (itemType) {
                case BATTERY -> "art/fusions/mechbot.png";
                case TOXIC_WASTE -> "art/fusions/toxicmech.png";
                case RADIOACTIVE_GOO -> "art/fusions/radioactivemech.png";
                case CRYOGEL -> "art/fusions/criomech.png";
                case CRYSTAL_SHARD -> "art/fusions/crystalmech.png";
                case NANOBOTS -> "art/fusions/nanomechbot.png";
            };
            case FUNGUS -> switch (itemType) {
                case BATTERY -> "art/fusions/electrofungy.png";
                case TOXIC_WASTE -> "art/fusions/toxicfungy.png";
                case RADIOACTIVE_GOO -> "art/fusions/radioactivefungy.png";
                case CRYOGEL -> "art/fusions/criofungy.png";
                case CRYSTAL_SHARD -> "art/fusions/crystalfungy.png";
                case NANOBOTS -> "art/fusions/nanofungy.png";
            };
        };
    }
}

