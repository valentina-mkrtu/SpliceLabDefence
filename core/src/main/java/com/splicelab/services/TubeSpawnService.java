package com.splicelab.services;

import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.SaveRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.level.LevelDefinition;

import java.util.ArrayList;
import java.util.HashMap;
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

    public List<SpawnChoice> buildSpawnBagForLevel(int levelNumber, int bagSize) {
        LevelDefinition level = levels.getLevel(levelNumber).orElse(null);
        if (level == null) return List.of();

        // If level defines an explicit 8-bag composition, repeat it.
        if (level.tubeSpawnBag8 != null && !level.tubeSpawnBag8.isEmpty()) {
            List<SpawnChoice> base = filterLocked(level.tubeSpawnBag8);
            if (!base.isEmpty()) {
                int size = Math.max(1, bagSize);
                List<SpawnChoice> out = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    out.add(base.get(i % base.size()));
                }
                shuffle(out);
                return out;
            }
        }

        List<EntityType> entities = new ArrayList<>();
        for (EntityType e : level.availableEntities) {
            if (saves.get().unlockedEntities.contains(e.name()) && definitions.getEntity(e).isPresent()) {
                entities.add(e);
            }
        }
        List<ItemType> items = new ArrayList<>();
        for (ItemType i : level.availableItems) {
            if (saves.get().unlockedItems.contains(i.name()) && definitions.getItem(i).isPresent()) {
                items.add(i);
            }
        }

        if (entities.isEmpty() && items.isEmpty()) return List.of();

        int size = Math.max(1, bagSize);
        int entityCount;
        int itemCount;
        if (!entities.isEmpty() && !items.isEmpty()) {
            entityCount = size / 2;
            itemCount = size - entityCount;
        } else if (!entities.isEmpty()) {
            entityCount = size;
            itemCount = 0;
        } else {
            entityCount = 0;
            itemCount = size;
        }

        List<SpawnChoice> out = new ArrayList<>(size);
        out.addAll(pickWithRepeatLimitEntities(entities, entityCount));
        out.addAll(pickWithRepeatLimitItems(items, itemCount));
        shuffle(out);
        return out;
    }

    private List<SpawnChoice> filterLocked(List<SpawnChoice> in) {
        List<SpawnChoice> out = new ArrayList<>();
        if (in == null) return out;
        for (SpawnChoice c : in) {
            if (c == null) continue;
            if (c.type() == SpawnChoice.Type.ENTITY) {
                EntityType e = c.entityType();
                if (e != null && saves.get().unlockedEntities.contains(e.name()) && definitions.getEntity(e).isPresent()) out.add(c);
            } else if (c.type() == SpawnChoice.Type.ITEM) {
                ItemType it = c.itemType();
                if (it != null && saves.get().unlockedItems.contains(it.name()) && definitions.getItem(it).isPresent()) out.add(c);
            }
        }
        return out;
    }

    private <T> void shuffle(List<T> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private List<SpawnChoice> pickWithRepeatLimitEntities(List<EntityType> options, int count) {
        List<SpawnChoice> out = new ArrayList<>(count);
        if (count <= 0) return out;
        if (options.isEmpty()) return out;

        HashMap<EntityType, Integer> used = new HashMap<>();
        for (int k = 0; k < count; k++) {
            EntityType picked = pickWithRepeatLimit(options, used, 2);
            out.add(SpawnChoice.entity(picked));
        }
        return out;
    }

    private List<SpawnChoice> pickWithRepeatLimitItems(List<ItemType> options, int count) {
        List<SpawnChoice> out = new ArrayList<>(count);
        if (count <= 0) return out;
        if (options.isEmpty()) return out;

        HashMap<ItemType, Integer> used = new HashMap<>();
        for (int k = 0; k < count; k++) {
            ItemType picked = pickWithRepeatLimit(options, used, 2);
            out.add(SpawnChoice.item(picked));
        }
        return out;
    }

    private <T> T pickWithRepeatLimit(List<T> options, HashMap<T, Integer> used, int maxRepeats) {
        if (options.size() == 1) {
            T only = options.get(0);
            used.put(only, used.getOrDefault(only, 0) + 1);
            return only;
        }

        // Prefer options under the repeat limit, but fall back safely.
        List<T> candidates = new ArrayList<>();
        for (T o : options) {
            if (used.getOrDefault(o, 0) < maxRepeats) candidates.add(o);
        }
        List<T> pool = candidates.isEmpty() ? options : candidates;
        T picked = pool.get(random.nextInt(pool.size()));
        used.put(picked, used.getOrDefault(picked, 0) + 1);
        return picked;
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
