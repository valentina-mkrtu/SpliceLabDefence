package com.splicelab.services;

import com.badlogic.gdx.math.MathUtils;

public final class NameGenerator {
    private static final String[] ENTITIES = {"Slime", "Mech", "Fungi", "Plasma", "Alien", "Egg"};
    private static final String[] ITEMS = {"Battery", "Toxic Waste", "Water Bottle", "Lamp"};

    public String generate() {
        String entity = ENTITIES[MathUtils.random(ENTITIES.length - 1)];
        String item = ITEMS[MathUtils.random(ITEMS.length - 1)];
        return entity + " " + item;
    }
}

