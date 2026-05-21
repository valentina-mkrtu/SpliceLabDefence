package com.splicelab.model.level;

public record LevelRewardDefinition(int coins, int dna) {
    public LevelRewardDefinition {
        coins = Math.max(0, coins);
        dna = Math.max(0, dna);
    }
}

