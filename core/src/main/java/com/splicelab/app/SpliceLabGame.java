package com.splicelab.app;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.splicelab.assets.AssetService;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.audio.AudioService;
import com.splicelab.data.BalanceRepository;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.GameDatabase;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.LocalizationRepository;
import com.splicelab.data.SaveRepository;
import com.splicelab.data.TutorialRepository;
import com.splicelab.data.UnlockRepository;
import com.splicelab.debug.DebugFlags;
import com.splicelab.screens.LoadingScreen;
import com.splicelab.services.AdRewardService;
import com.splicelab.services.BoostInventoryService;
import com.splicelab.services.EconomyService;
import com.splicelab.services.FusionService;
import com.splicelab.services.FusionUnlockService;
import com.splicelab.services.LevelService;
import com.splicelab.services.RandomService;
import com.splicelab.services.RewardService;
import com.splicelab.services.TubeSpawnService;
import com.splicelab.services.UnlockService;
import com.splicelab.telemetry.TelemetryBus;

public final class SpliceLabGame extends Game {
    private SpriteBatch batch;
    private AssetService assets;
    private AudioService audio;
    private Skin sharedSkin;
    private GameContext context;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new AssetService();
        audio = new AudioService();

        GameDatabase.LoadedData loaded = new GameDatabase().loadAllWithFallbacks();
        GameConfig config = loaded.config;

        RandomService randomService = new RandomService(DebugFlags.SEEDED_RANDOM ? 1337L : null);

        DefinitionRepository definitionRepository = loaded.definitions;
        LevelRepository levelRepository = loaded.levels;
        BalanceRepository balanceRepository = loaded.balance;
        UnlockRepository unlockRepository = loaded.unlocks;
        TutorialRepository tutorialRepository = loaded.tutorial;
        LocalizationRepository localizationRepository = loaded.localization;

        SaveRepository saveRepository = new SaveRepository(config);
        saveRepository.loadOrCreate();

        EconomyService economyService = new EconomyService(saveRepository);
        BoostInventoryService boostInventoryService = new BoostInventoryService(saveRepository);
        UnlockService unlockService = new UnlockService(saveRepository, config);
        RewardService rewardService = new RewardService(saveRepository);
        FusionService fusionService = new FusionService(definitionRepository);
        FusionUnlockService fusionUnlockService = new FusionUnlockService(saveRepository);
        TubeSpawnService tubeSpawnService = new TubeSpawnService(definitionRepository, levelRepository, saveRepository, randomService);
        LevelService levelService = new LevelService(levelRepository);
        AdRewardService adRewardService = new AdRewardService();
        TelemetryBus telemetry = new TelemetryBus();

        // T-3.1: build the shared skin once here; dispose in SpliceLabGame.dispose().
        sharedSkin = PlaceholderSkinFactory.create();

        context = new GameContext(
                assets,
                audio,
                sharedSkin,
                definitionRepository,
                levelRepository,
                saveRepository,
                economyService,
                boostInventoryService,
                unlockService,
                rewardService,
                fusionService,
                fusionUnlockService,
                tubeSpawnService,
                levelService,
                randomService,
                adRewardService,
                config,
                telemetry
        );

        adRewardService.setTelemetry(telemetry);

        setScreen(new LoadingScreen(this, context));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GameContext getContext() {
        return context;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assets != null) assets.dispose();
        if (audio != null) audio.dispose();
        if (batch != null) batch.dispose();
        if (sharedSkin != null) sharedSkin.dispose(); // T-3.1
        Gdx.app.log(AppConstants.LOG_TAG, "Game disposed");
    }
}
