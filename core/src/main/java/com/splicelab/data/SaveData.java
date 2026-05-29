package com.splicelab.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SaveData {
    public int schemaVersion = 1;
    public String playerId = "";
    public String playerName = "";
    public int playerLevel = 1;
    public int xp = 0;
    public int currentLevel = 1;
    public int dna = 0;   // was: coins  (primary currency, earned + spent in shop)
    public int cry = 0;   // was: dna    (secondary currency)

    public int dayStreak = 0;
    public int totalFusionsUnlocked = 0;

    public final Set<String> unlockedFusions = new HashSet<>();

    public final Set<String> unlockedEntities = new HashSet<>();
    public final Set<String> unlockedItems = new HashSet<>();
    // key = ShopDialog.PurchaseType.name(), value = count owned
    public final Map<String, Integer> boostInventory = new HashMap<>();
    public int unlockedConveyorSlotsLeft = 1;
    public int unlockedConveyorSlotsRight = 1;

    public final Set<Integer> completedLevels = new HashSet<>();
    public final Set<String> tutorialCompletedFlags = new HashSet<>();

    public boolean musicEnabled = true;
    public boolean sfxEnabled = true;

    // Endless mode: best survival time (seconds).
    public float endlessBestSurvivalSeconds = 0f;

    public int getBoostCount(String typeName) {
        return boostInventory.getOrDefault(typeName, 0);
    }

    public void addBoost(String typeName, int delta) {
        boostInventory.put(typeName, Math.max(0, getBoostCount(typeName) + delta));
    }

    public boolean consumeBoost(String typeName) {
        int c = getBoostCount(typeName);
        if (c <= 0) return false;
        boostInventory.put(typeName, c - 1);
        return true;
    }
}
