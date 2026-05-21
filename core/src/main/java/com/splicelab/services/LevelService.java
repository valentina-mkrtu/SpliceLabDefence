package com.splicelab.services;

import com.splicelab.data.LevelRepository;
import com.splicelab.model.level.LevelDefinition;

public final class LevelService {
    private final LevelRepository levels;

    public LevelService(LevelRepository levels) {
        this.levels = levels;
    }

    public LevelDefinition requireLevel(int levelNumber) {
        return levels.getLevel(levelNumber).orElseThrow(() -> new IllegalArgumentException("Missing level " + levelNumber));
    }
}

