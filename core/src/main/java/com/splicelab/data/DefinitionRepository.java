package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.model.AttackElement;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyAttackDefinition;
import com.splicelab.model.enemy.EnemyDefinition;
import com.splicelab.model.enemy.EnemyType;
import com.splicelab.model.ingredient.EntityDefinition;
import com.splicelab.model.ingredient.FusionDefinition;
import com.splicelab.model.ingredient.ItemDefinition;
import com.splicelab.model.stats.CombatStats;
import com.splicelab.model.stats.SpecialAttackData;
import com.splicelab.model.stats.StatBlock;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public final class DefinitionRepository {
    private final EnumMap<EntityType, EntityDefinition> entities = new EnumMap<>(EntityType.class);
    private final EnumMap<ItemType, ItemDefinition> items = new EnumMap<>(ItemType.class);
    private final Map<String, FusionDefinition> fusions = new HashMap<>();
    private final EnumMap<EnemyType, EnemyDefinition> enemies = new EnumMap<>(EnemyType.class);

    private DefinitionRepository() {
    }

    public static final class MutableDefinitions {
        public final EnumMap<EntityType, EntityDefinition> entities = new EnumMap<>(EntityType.class);
        public final EnumMap<ItemType, ItemDefinition> items = new EnumMap<>(ItemType.class);
        public final Map<String, FusionDefinition> fusions = new HashMap<>();
        public final EnumMap<EnemyType, EnemyDefinition> enemies = new EnumMap<>(EnemyType.class);

        public DefinitionRepository freeze() {
            DefinitionRepository repo = new DefinitionRepository();
            repo.entities.putAll(entities);
            repo.items.putAll(items);
            repo.fusions.putAll(fusions);
            repo.enemies.putAll(enemies);
            return repo;
        }
    }

    public Optional<EntityDefinition> getEntity(EntityType type) {
        return Optional.ofNullable(entities.get(type));
    }

    public Optional<ItemDefinition> getItem(ItemType type) {
        return Optional.ofNullable(items.get(type));
    }

    public Optional<FusionDefinition> getFusion(EntityType entityType, ItemType itemType) {
        return Optional.ofNullable(fusions.get(fusionKey(entityType, itemType)));
    }

    public List<FusionDefinition> allFusions() {
        List<FusionDefinition> out = new ArrayList<>();
        for (EntityType e : EntityType.values()) {
            for (ItemType i : ItemType.values()) {
                getFusion(e, i).ifPresent(out::add);
            }
        }
        return out;
    }

    public Optional<EnemyDefinition> getEnemy(EnemyType type) {
        return Optional.ofNullable(enemies.get(type));
    }

    public int getEnemyThreat(EnemyType type) {
        EnemyDefinition def = enemies.get(type);
        if (def == null) return 0;
        // Threat is stored as rewardWeight for now (XML uses threat/rewardWeight).
        return Math.max(0, Math.round(def.rewardWeight));
    }

    public static DefinitionRepository createStarter() {
        MutableDefinitions m = new MutableDefinitions();

        m.entities.put(EntityType.SLIME, new EntityDefinition(
                EntityType.SLIME,
                "Slime",
                new CombatStats(160, 10, 1.25f, 0.35f, 0.15f),
                new SpecialAttackData("sticky", "Sticky double-hit (prototype)"),
                AttackElement.NEUTRAL,
                "A tough blob that wins by outlasting.",
                "ENTITY_SLIME_NAME",
                "ENTITY_SLIME_SHORT",
                "ENTITY_SLIME_LONG",
                "",
                "",
                "",
                "",
                true
        ));
        m.entities.put(EntityType.MECH, new EntityDefinition(
                EntityType.MECH,
                "Mech",
                new CombatStats(110, 22, 1.5f, 0.2f, 0.1f),
                new SpecialAttackData("precision", "Precision double-damage (prototype)"),
                AttackElement.NEUTRAL,
                "Hard-hitting chassis with stable output.",
                "ENTITY_MECH_NAME",
                "ENTITY_MECH_SHORT",
                "ENTITY_MECH_LONG",
                "",
                "",
                "",
                "",
                true
        ));
        m.entities.put(EntityType.FUNGUS, new EntityDefinition(
                EntityType.FUNGUS,
                "Fungus",
                new CombatStats(120, 16, 1.35f, 0.3f, 0.2f),
                new SpecialAttackData("spores", "Spore poison (prototype)"),
                AttackElement.SPORE,
                "Balanced attacker with nasty spores.",
                "ENTITY_FUNGUS_NAME",
                "ENTITY_FUNGUS_SHORT",
                "ENTITY_FUNGUS_LONG",
                "",
                "",
                "",
                "",
                true
        ));

        m.items.put(ItemType.BATTERY, new ItemDefinition(
                ItemType.BATTERY,
                "Battery",
                new StatBlock(0, 8),
                0.08f,
                -0.03f,
                AttackElement.ELECTRIC,
                0f,
                "+ATK and sparks.",
                "ITEM_BATTERY_NAME",
                "ITEM_BATTERY_SHORT",
                "ITEM_BATTERY_LONG",
                "",
                "",
                "",
                true
        ));
        m.items.put(ItemType.TOXIC_WASTE, new ItemDefinition(
                ItemType.TOXIC_WASTE,
                "Toxic Waste",
                new StatBlock(-10, 6),
                0.05f,
                0.02f,
                AttackElement.TOXIC,
                0f,
                "Poison identity, risky HP.",
                "ITEM_TOXIC_WASTE_NAME",
                "ITEM_TOXIC_WASTE_SHORT",
                "ITEM_TOXIC_WASTE_LONG",
                "",
                "",
                "",
                true
        ));
        m.items.put(ItemType.RADIOACTIVE_GOO, new ItemDefinition(
                ItemType.RADIOACTIVE_GOO,
                "Radioactive Goo",
                new StatBlock(-20, 14),
                0.02f,
                0.25f,
                AttackElement.RADIOACTIVE,
                0f,
                "Huge ATK, huge variance.",
                "ITEM_RADIOACTIVE_GOO_NAME",
                "ITEM_RADIOACTIVE_GOO_SHORT",
                "ITEM_RADIOACTIVE_GOO_LONG",
                "",
                "",
                "",
                true
        ));
        m.items.put(ItemType.CRYOGEL, new ItemDefinition(
                ItemType.CRYOGEL,
                "Cryogel",
                new StatBlock(25, 0),
                0.03f,
                -0.05f,
                AttackElement.CRYO,
                0f,
                "Tankiness and slow identity.",
                "ITEM_CRYOGEL_NAME",
                "ITEM_CRYOGEL_SHORT",
                "ITEM_CRYOGEL_LONG",
                "",
                "",
                "",
                true
        ));
        m.items.put(ItemType.CRYSTAL_SHARD, new ItemDefinition(
                ItemType.CRYSTAL_SHARD,
                "Crystal Shard",
                new StatBlock(0, 10),
                0.1f,
                0.05f,
                AttackElement.CRYSTAL,
                0f,
                "Piercing/crit identity.",
                "ITEM_CRYSTAL_SHARD_NAME",
                "ITEM_CRYSTAL_SHARD_SHORT",
                "ITEM_CRYSTAL_SHARD_LONG",
                "",
                "",
                "",
                true
        ));
        m.items.put(ItemType.NANOBOTS, new ItemDefinition(
                ItemType.NANOBOTS,
                "Nanobots",
                new StatBlock(30, 2),
                0.04f,
                -0.08f,
                AttackElement.NEUTRAL,
                0f,
                "Repair/consistency identity.",
                "ITEM_NANOBOTS_NAME",
                "ITEM_NANOBOTS_SHORT",
                "ITEM_NANOBOTS_LONG",
                "",
                "",
                "",
                true
        ));

        // Starter fusions: defined per pair, display/ability can be tuned later.
        for (EntityType e : EntityType.values()) {
            for (ItemType i : ItemType.values()) {
                m.fusions.put(fusionKey(e, i), new FusionDefinition(
                        fusionKey(e, i),
                        e,
                        i,
                        prettyFusionName(e, i),
                        "proj_basic",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                ));
            }
        }

        m.enemies.put(EnemyType.SMUGGLER_GRUNT, new EnemyDefinition(
                EnemyType.SMUGGLER_GRUNT,
                "Smuggler Grunt",
                200,
                new EnemyAttackDefinition(8, 1.2f),
                "proj_enemy",
                1f
        ));

        Gdx.app.log(AppConstants.LOG_TAG, "Loaded starter definitions");
        return m.freeze();
    }

    private static String fusionKey(EntityType entityType, ItemType itemType) {
        return Objects.requireNonNull(entityType).name() + "+" + Objects.requireNonNull(itemType).name();
    }

    private static String prettyFusionName(EntityType e, ItemType i) {
        return e.name().charAt(0) + e.name().substring(1).toLowerCase() + " + " + i.name().charAt(0) + i.name().substring(1).toLowerCase();
    }
}
