package com.splicelab.model.stats;

public record StatBlock(int hp, int atk) {
    public StatBlock {
        hp = Math.max(0, hp);
        atk = Math.max(0, atk);
    }
}

