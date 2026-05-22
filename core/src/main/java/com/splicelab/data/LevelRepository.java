package com.splicelab.data;

import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyType;
import com.splicelab.model.level.LevelDefinition;
import com.splicelab.model.level.LevelRewardDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LevelRepository {
    private final Map<Integer, LevelDefinition> levels = new HashMap<>();

    private LevelRepository() {
    }

    public static final class MutableLevels {
        public final Map<Integer, LevelDefinition> levels = new HashMap<>();

        public LevelRepository freeze() {
            LevelRepository repo = new LevelRepository();
            repo.levels.putAll(levels);
            return repo;
        }
    }

    public Optional<LevelDefinition> getLevel(int levelNumber) {
        return Optional.ofNullable(levels.get(levelNumber));
    }

    public static LevelRepository createStarter() {
        MutableLevels m = new MutableLevels();
        m.levels.put(1, new LevelDefinition(
                1,
                180f,
                250,
                1.5f,
                5,
                1,
                1,
                List.of(EntityType.SLIME, EntityType.MECH, EntityType.FUNGUS),
                List.of(ItemType.BATTERY, ItemType.TOXIC_WASTE, ItemType.CRYOGEL, ItemType.CRYSTAL_SHARD),
                List.of(new LevelDefinition.EnemySpawnEntry(EnemyType.SMUGGLER_GRUNT, 1f)),
                List.of(),
                1.0f,
                1.0f,
                2.2f,
                List.of(),
                LevelRewardDefinition.of(50, 5),
                "Smugglers breached the vents. Fuse fast.",
                null
        ));
        return m.freeze();
    }
}
