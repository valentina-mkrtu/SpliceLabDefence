package com.splicelab.services;

import com.splicelab.data.SaveRepository;

public final class FusionUnlockService {
    private final SaveRepository saves;

    public FusionUnlockService(SaveRepository saves) {
        this.saves = saves;
    }

    public boolean isUnlocked(String fusionId) {
        if (fusionId == null || fusionId.isBlank()) return false;
        return saves.get().unlockedFusions.contains(fusionId);
    }

    public boolean unlock(String fusionId) {
        if (fusionId == null || fusionId.isBlank()) return false;
        boolean added = saves.get().unlockedFusions.add(fusionId);
        if (added) {
            saves.get().totalFusionsUnlocked = Math.max(saves.get().totalFusionsUnlocked, saves.get().unlockedFusions.size());
            saves.save();
        }
        return added;
    }
}

