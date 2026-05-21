package com.splicelab.model.level;

public record LevelObjectiveDefinition(String id) {
    public LevelObjectiveDefinition {
        id = id == null ? "" : id;
    }
}

