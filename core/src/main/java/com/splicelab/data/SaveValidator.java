package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameConfig;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;

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

        data.playerLevel = Math.max(1, data.playerLevel);
        data.xp = Math.max(0, data.xp);
        data.currentLevel = Math.max(1, data.currentLevel);
        data.coins = Math.max(0, data.coins);
        data.dna = Math.max(0, data.dna);

        data.unlockedConveyorSlotsLeft = clamp(data.unlockedConveyorSlotsLeft, 0, config.maxConveyorSlotsPerSide);
        data.unlockedConveyorSlotsRight = clamp(data.unlockedConveyorSlotsRight, 0, config.maxConveyorSlotsPerSide);

        removeInvalidEnumNames(data.unlockedEntities, EntityType.class);
        removeInvalidEnumNames(data.unlockedItems, ItemType.class);

        ensureStarterUnlocks(data);
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
        return data;
    }

    private void ensureStarterUnlocks(SaveData data) {
        data.unlockedEntities.add(EntityType.SLIME.name());
        data.unlockedItems.add(ItemType.BATTERY.name());
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
}

