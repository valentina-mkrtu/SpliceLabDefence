package com.splicelab.services;

import com.splicelab.app.GameConfig;
import com.splicelab.data.SaveRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;

public final class UnlockService {
    private final SaveRepository saves;
    private final GameConfig config;

    public UnlockService(SaveRepository saves, GameConfig config) {
        this.saves = saves;
        this.config = config;
    }

    public boolean isEntityUnlocked(EntityType type) {
        if (type == null) return false;
        return saves.get().unlockedEntities.contains(type.name());
    }

    public boolean isItemUnlocked(ItemType type) {
        if (type == null) return false;
        return saves.get().unlockedItems.contains(type.name());
    }

    public boolean unlockEntity(EntityType type) {
        if (type == null) return false;
        boolean added = saves.get().unlockedEntities.add(type.name());
        if (added) saves.save();
        return added;
    }

    public boolean unlockItem(ItemType type) {
        if (type == null) return false;
        boolean added = saves.get().unlockedItems.add(type.name());
        if (added) saves.save();
        return added;
    }

    public boolean isConveyorSlotUnlocked(boolean leftSide, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= config.maxConveyorSlotsPerSide) return false;
        return leftSide ? slotIndex < saves.get().unlockedConveyorSlotsLeft : slotIndex < saves.get().unlockedConveyorSlotsRight;
    }
}

