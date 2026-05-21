package com.splicelab.model.ingredient;

import java.util.Objects;

public record IngredientId(String value) {
    public IngredientId {
        value = value == null ? "" : value.trim();
    }

    public boolean isValid() {
        return !value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }

    public static IngredientId of(String value) {
        return new IngredientId(value);
    }
}

