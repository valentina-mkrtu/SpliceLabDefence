package com.splicelab.data;

import java.util.HashSet;
import java.util.Set;

public final class SaveData {
    public int schemaVersion = 1;
    public int playerLevel = 1;
    public int xp = 0;
    public int currentLevel = 1;
    public int coins = 0;
    public int dna = 0;

    public final Set<String> unlockedEntities = new HashSet<>();
    public final Set<String> unlockedItems = new HashSet<>();
    public int unlockedConveyorSlotsLeft = 1;
    public int unlockedConveyorSlotsRight = 1;

    public final Set<Integer> completedLevels = new HashSet<>();
    public final Set<String> tutorialCompletedFlags = new HashSet<>();

    public boolean musicEnabled = true;
    public boolean sfxEnabled = true;
}

