package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameConfig;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;

import java.util.UUID;

import java.util.Iterator;

public final class SaveValidator {
    private final GameConfig config;

    public SaveValidator(GameConfig config) {
        this.config = config;
    }

    public SaveData validateAndRepair(SaveData data) {
        if (data == null) {
            Gdx.app.error(AppConstants.LOG_TAG, "SaveData was null; creating default");
            return defaultSave();
        }

        if (data.schemaVersion <= 0 || data.schemaVersion > config.saveSchemaVersion) {
            Gdx.app.error(AppConstants.LOG_TAG, "Invalid schemaVersion=" + data.schemaVersion + ", repairing");
            data.schemaVersion = config.saveSchemaVersion;
        }

        data.playerLevel = clamp(data.playerLevel, 1, GameConfig.MAX_PLAYER_LEVEL);
        data.xp = clamp(data.xp, 0, GameConfig.MAX_XP);
        data.currentLevel = clamp(data.currentLevel, 1, GameConfig.MAX_CURRENT_LEVEL);
        data.coins = clamp(data.coins, 0, GameConfig.MAX_COINS);
        data.dna = clamp(data.dna, 0, GameConfig.MAX_DNA);
        data.endlessBestSurvivalSeconds = Math.max(0f, data.endlessBestSurvivalSeconds);

        data.unlockedConveyorSlotsLeft = clamp(data.unlockedConveyorSlotsLeft, 0, config.maxConveyorSlotsPerSide);
        data.unlockedConveyorSlotsRight = clamp(data.unlockedConveyorSlotsRight, 0, config.maxConveyorSlotsPerSide);

        removeInvalidEnumNames(data.unlockedEntities, EntityType.class);
        removeInvalidEnumNames(data.unlockedItems, ItemType.class);
        sanitizeStringSet(data.ownedShopPurchases);

        ensureStarterUnlocks(data);
        ensureIdentity(data);
        return data;
    }

    public SaveData defaultSave() {
        SaveData data = new SaveData();
        data.schemaVersion = config.saveSchemaVersion;
        data.playerLevel = 1;
        data.currentLevel = 1;
        data.coins = 0;
        data.dna = 0;
        data.unlockedConveyorSlotsLeft = 1;
        data.unlockedConveyorSlotsRight = 1;
        ensureStarterUnlocks(data);
        ensureIdentity(data);
        return data;
    }

    private void ensureIdentity(SaveData data) {
        if (data.playerId == null || data.playerId.isBlank()) {
            data.playerId = UUID.randomUUID().toString();
        }
        if (data.playerName == null || data.playerName.isBlank()) {
            data.playerName = generateRandomPlayerName(data.playerId);
        }
    }

    private String generateRandomPlayerName(String seed) {
        String[] adj = {"Fuzzy", "Brave", "Wired", "Neon", "Mossy", "Toxic", "Icy", "Crystal", "Nano", "Rusty"};
        String[] noun = {"Tech", "Lab", "Splicer", "Gizmo", "Slime", "Mech", "Fungus", "Runner", "Maker", "Hunter"};
        int h = seed == null ? 0 : seed.hashCode();
        int a = Math.floorMod(h, adj.length);
        int b = Math.floorMod(h / 31, noun.length);
        int num = Math.floorMod(h / 131, 900) + 100;
        return adj[a] + noun[b] + num;
    }

    private void ensureStarterUnlocks(SaveData data) {
        data.unlockedEntities.add(EntityType.SLIME.name());
        data.unlockedEntities.add(EntityType.MECH.name());
        data.unlockedEntities.add(EntityType.FUNGUS.name());
        data.unlockedItems.add(ItemType.BATTERY.name());
        data.unlockedItems.add(ItemType.TOXIC_WASTE.name());
        data.unlockedItems.add(ItemType.CRYOGEL.name());
        data.unlockedItems.add(ItemType.CRYSTAL_SHARD.name());
        data.unlockedItems.add(ItemType.RADIOACTIVE_GOO.name());
        data.unlockedItems.add(ItemType.NANOBOTS.name());
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static <E extends Enum<E>> void removeInvalidEnumNames(Iterable<String> set, Class<E> enumClass) {
        if (!(set instanceof java.util.Set<?> s)) return;
        Iterator<?> it = s.iterator();
        while (it.hasNext()) {
            Object v = it.next();
            if (!(v instanceof String str)) {
                it.remove();
                continue;
            }
            try {
                Enum.valueOf(enumClass, str);
            } catch (Exception ex) {
                it.remove();
            }
        }
    }

    private static void sanitizeStringSet(Iterable<String> set) {
        if (!(set instanceof java.util.Set<?> s)) return;
        Iterator<?> it = s.iterator();
        while (it.hasNext()) {
            Object v = it.next();
            if (!(v instanceof String str) || str.isBlank()) {
                it.remove();
            }
        }
    }
}
