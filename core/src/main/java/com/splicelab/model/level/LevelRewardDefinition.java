package com.splicelab.model.level;

public record LevelRewardDefinition(int coins, int dna, int firstWinBonusCoins, int firstWinBonusDna) {
    public LevelRewardDefinition {
        coins = Math.max(0, coins);
        dna = Math.max(0, dna);
        firstWinBonusCoins = Math.max(0, firstWinBonusCoins);
        firstWinBonusDna = Math.max(0, firstWinBonusDna);
    }

    public static LevelRewardDefinition of(int coins, int dna) {
        return new LevelRewardDefinition(coins, dna, 0, 0);
    }
}
