package com.splicelab.model.stats;

public record SpecialAttackData(String id, String description) {
    public SpecialAttackData {
        id = id == null ? "" : id;
        description = description == null ? "" : description;
    }
}

