package com.splicelab.services;

import com.splicelab.data.SaveRepository;
import com.splicelab.model.CurrencyType;
import com.splicelab.model.level.LevelRewardDefinition;

public final class RewardService {
    private final SaveRepository saves;

    public RewardService(SaveRepository saves) {
        this.saves = saves;
    }

    public void apply(LevelRewardDefinition reward, boolean doubleReward) {
        if (reward == null) return;
        int mult = doubleReward ? 2 : 1;
        saves.get().coins = Math.max(0, saves.get().coins + reward.coins() * mult);
        saves.get().dna = Math.max(0, saves.get().dna + reward.dna() * mult);
        saves.save();
    }
}

