package com.splicelab.model.ingredient;

import com.splicelab.model.IngredientKind;

public sealed interface IngredientInstance permits FusionInstance, SimpleIngredientInstance {
    String instanceId();

    IngredientKind kind();
}

