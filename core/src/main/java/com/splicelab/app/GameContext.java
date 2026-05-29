package com.splicelab.app;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.splicelab.assets.AssetService;
import com.splicelab.audio.AudioService;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.SaveRepository;
import com.splicelab.services.AdRewardService;
import com.splicelab.services.EconomyService;
import com.splicelab.services.FusionService;
import com.splicelab.services.FusionUnlockService;
import com.splicelab.services.LevelService;
import com.splicelab.services.RandomService;
import com.splicelab.services.RewardService;
import com.splicelab.services.TubeSpawnService;
import com.splicelab.services.UnlockService;
import com.splicelab.telemetry.TelemetryBus;

public final class GameContext {
    public final AssetService assets;
    public final AudioService audio;
    /**
     * Shared UI skin — built once in {@link SpliceLabGame#create()} and disposed in
     * {@link SpliceLabGame#dispose()}.  Views and screens must NOT dispose this skin.  (T-3.1)
     */
    public final Skin skin;
    public final DefinitionRepository definitions;
    public final LevelRepository levels;
    public final SaveRepository saves;
    public final EconomyService economy;
    public final UnlockService unlocks;
    public final RewardService rewards;
    public final FusionService fusionService;
    public final FusionUnlockService fusionUnlocks;
    public final TubeSpawnService tubeSpawnService;
    public final LevelService levelService;
    public final RandomService random;
    public final AdRewardService adRewardService;
    public final GameConfig config;
    public final TelemetryBus telemetry;

    public GameContext(
            AssetService assets,
            AudioService audio,
            Skin skin,
            DefinitionRepository definitions,
            LevelRepository levels,
            SaveRepository saves,
            EconomyService economy,
            UnlockService unlocks,
            RewardService rewards,
            FusionService fusionService,
            FusionUnlockService fusionUnlocks,
            TubeSpawnService tubeSpawnService,
            LevelService levelService,
            RandomService random,
            AdRewardService adRewardService,
            GameConfig config,
            TelemetryBus telemetry
    ) {
        this.assets = assets;
        this.audio = audio;
        this.skin = skin;
        this.definitions = definitions;
        this.levels = levels;
        this.saves = saves;
        this.economy = economy;
        this.unlocks = unlocks;
        this.rewards = rewards;
        this.fusionService = fusionService;
        this.fusionUnlocks = fusionUnlocks;
        this.tubeSpawnService = tubeSpawnService;
        this.levelService = levelService;
        this.random = random;
        this.adRewardService = adRewardService;
        this.config = config;
        this.telemetry = telemetry == null ? new TelemetryBus() : telemetry;
    }
}
