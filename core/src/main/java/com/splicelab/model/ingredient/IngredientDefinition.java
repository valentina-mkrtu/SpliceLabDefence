package com.splicelab.model.ingredient;

import com.splicelab.model.IngredientKind;

public abstract class IngredientDefinition {
    public final IngredientKind kind;
    public final String displayName;
    public final String description;

    protected IngredientDefinition(IngredientKind kind, String displayName, String description) {
        this.kind = kind;
        this.displayName = displayName == null ? "" : displayName;
        this.description = description == null ? "" : description;
    }
}

