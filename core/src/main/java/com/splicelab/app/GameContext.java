package com.splicelab.app;

import com.splicelab.assets.AssetService;
import com.splicelab.audio.AudioService;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.SaveRepository;
import com.splicelab.services.AdRewardService;
import com.splicelab.services.EconomyService;
import com.splicelab.services.FusionService;
import com.splicelab.services.LevelService;
import com.splicelab.services.RandomService;
import com.splicelab.services.RewardService;
import com.splicelab.services.TubeSpawnService;
import com.splicelab.services.UnlockService;

public final class GameContext {
    public final AssetService assets;
    public final AudioService audio;
    public final DefinitionRepository definitions;
    public final LevelRepository levels;
    public final SaveRepository saves;
    public final EconomyService economy;
    public final UnlockService unlocks;
    public final RewardService rewards;
    public final FusionService fusionService;
    public final TubeSpawnService tubeSpawnService;
    public final LevelService levelService;
    public final RandomService random;
    public final AdRewardService adRewardService;
    public final GameConfig config;

    public GameContext(
            AssetService assets,
            AudioService audio,
            DefinitionRepository definitions,
            LevelRepository levels,
            SaveRepository saves,
            EconomyService economy,
            UnlockService unlocks,
            RewardService rewards,
            FusionService fusionService,
            TubeSpawnService tubeSpawnService,
            LevelService levelService,
            RandomService random,
            AdRewardService adRewardService,
            GameConfig config
    ) {
        this.assets = assets;
        this.audio = audio;
        this.definitions = definitions;
        this.levels = levels;
        this.saves = saves;
        this.economy = economy;
        this.unlocks = unlocks;
        this.rewards = rewards;
        this.fusionService = fusionService;
        this.tubeSpawnService = tubeSpawnService;
        this.levelService = levelService;
        this.random = random;
        this.adRewardService = adRewardService;
        this.config = config;
    }
}

