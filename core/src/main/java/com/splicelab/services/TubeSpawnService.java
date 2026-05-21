package com.splicelab.services;

import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.SaveRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.level.LevelDefinition;

import java.util.ArrayList;
import java.util.List;

public final class TubeSpawnService {
    private final DefinitionRepository definitions;
    private final LevelRepository levels;
    private final SaveRepository saves;
    private final RandomService random;

    public TubeSpawnService(DefinitionRepository definitions, LevelRepository levels, SaveRepository saves, RandomService random) {
        this.definitions = definitions;
        this.levels = levels;
        this.saves = saves;
        this.random = random;
    }

    public SpawnChoice chooseSpawnForLevel(int levelNumber) {
        LevelDefinition level = levels.getLevel(levelNumber).orElse(null);
        if (level == null) return SpawnChoice.none();

        List<SpawnChoice> choices = new ArrayList<>();
        for (EntityType e : level.availableEntities) {
            if (saves.get().unlockedEntities.contains(e.name()) && definitions.getEntity(e).isPresent()) {
                choices.add(SpawnChoice.entity(e));
            }
        }
        for (ItemType i : level.availableItems) {
            if (saves.get().unlockedItems.contains(i.name()) && definitions.getItem(i).isPresent()) {
                choices.add(SpawnChoice.item(i));
            }
        }
        if (choices.isEmpty()) return SpawnChoice.none();
        return choices.get(random.nextInt(choices.size()));
    }

    public record SpawnChoice(Type type, EntityType entityType, ItemType itemType) {
        public enum Type { NONE, ENTITY, ITEM }

        public static SpawnChoice none() {
            return new SpawnChoice(Type.NONE, null, null);
        }

        public static SpawnChoice entity(EntityType e) {
            return new SpawnChoice(Type.ENTITY, e, null);
        }

        public static SpawnChoice item(ItemType i) {
            return new SpawnChoice(Type.ITEM, null, i);
        }
    }
}

