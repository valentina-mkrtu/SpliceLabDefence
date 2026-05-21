package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameConfig;
import com.splicelab.data.xml.AdsRewardXmlParser;
import com.splicelab.data.xml.DataValidationReport;
import com.splicelab.data.xml.DataValidator;
import com.splicelab.data.xml.EconomyXmlParser;
import com.splicelab.data.xml.EnemyXmlParser;
import com.splicelab.data.xml.EntityXmlParser;
import com.splicelab.data.xml.FusionXmlParser;
import com.splicelab.data.xml.GameConfigXmlParser;
import com.splicelab.data.xml.ItemXmlParser;
import com.splicelab.data.xml.LevelXmlParser;
import com.splicelab.data.xml.LocalizationXmlParser;
import com.splicelab.data.xml.TutorialXmlParser;
import com.splicelab.data.xml.UnlockXmlParser;
import com.splicelab.data.xml.XmlDataLoader;
import com.splicelab.data.xml.XmlParseException;
import com.splicelab.model.level.LevelDefinition;

public final class GameDatabase {
    public static final class LoadedData {
        public final GameConfig config;
        public final DefinitionRepository definitions;
        public final LevelRepository levels;
        public final BalanceRepository balance;
        public final UnlockRepository unlocks;
        public final TutorialRepository tutorial;
        public final LocalizationRepository localization;
        public final DataValidationReport report;

        public LoadedData(
                GameConfig config,
                DefinitionRepository definitions,
                LevelRepository levels,
                BalanceRepository balance,
                UnlockRepository unlocks,
                TutorialRepository tutorial,
                LocalizationRepository localization,
                DataValidationReport report
        ) {
            this.config = config;
            this.definitions = definitions;
            this.levels = levels;
            this.balance = balance;
            this.unlocks = unlocks;
            this.tutorial = tutorial;
            this.localization = localization;
            this.report = report;
        }
    }

    private final XmlDataLoader loader = new XmlDataLoader();
    private LoadedData lastLoaded;

    public LoadedData loadAllWithFallbacks() {
        DataValidationReport report = new DataValidationReport();

        GameConfig configFallback = GameConfig.defaultConfig();
        GameConfig config = parseConfig(report, configFallback);

        DefinitionRepository definitionsFallback = DefinitionRepository.createStarter();
        LevelRepository levelsFallback = LevelRepository.createStarter();
        BalanceRepository balanceFallback = BalanceRepository.createStarter();
        UnlockRepository unlockFallback = UnlockRepository.createStarter(config.maxConveyorSlotsPerSide);
        TutorialRepository tutorialFallback = TutorialRepository.createStarter();
        LocalizationRepository localizationFallback = LocalizationRepository.createStarter();

        DefinitionRepository definitions = loadDefinitions(report, definitionsFallback);
        LevelRepository levels = loadLevels(report, levelsFallback);
        BalanceRepository balance = loadEconomy(report, balanceFallback);
        UnlockRepository unlocks = loadUnlocks(report, unlockFallback);
        TutorialRepository tutorial = loadTutorial(report, tutorialFallback);
        LocalizationRepository localization = loadLocalization(report, localizationFallback);
        loadAdsRewards(report);

        DataValidationReport validation = new DataValidator().validate(definitions, levels, balance, unlocks, tutorial, localization);
        for (var issue : validation.getIssues()) {
            report.add(issue.severity(), issue.source(), issue.message());
        }

        lastLoaded = new LoadedData(config, definitions, levels, balance, unlocks, tutorial, localization, report);
        logReport(lastLoaded);
        return lastLoaded;
    }

    private GameConfig parseConfig(DataValidationReport report, GameConfig fallback) {
        try {
            var root = loader.loadRoot("data/game_config.xml");
            return new GameConfigXmlParser().parse(root, report, fallback);
        } catch (XmlParseException ex) {
            report.warn("data/game_config.xml", ex.getMessage());
            return fallback;
        }
    }

    private DefinitionRepository loadDefinitions(DataValidationReport report, DefinitionRepository fallback) {
        DefinitionRepository.MutableDefinitions m = new DefinitionRepository.MutableDefinitions();
        // Start from fallback so partial XML can override.
        m.entities.putAll(extractMutable(fallback).entities);
        m.items.putAll(extractMutable(fallback).items);
        m.fusions.putAll(extractMutable(fallback).fusions);
        m.enemies.putAll(extractMutable(fallback).enemies);

        try {
            new EntityXmlParser().parseInto(loader.loadRoot("data/entities.xml"), m, report);
        } catch (Exception ex) {
            report.warn("data/entities.xml", ex.getMessage());
        }
        try {
            new ItemXmlParser().parseInto(loader.loadRoot("data/items.xml"), m, report);
        } catch (Exception ex) {
            report.warn("data/items.xml", ex.getMessage());
        }
        try {
            new FusionXmlParser().parseInto(loader.loadRoot("data/fusions.xml"), m, report);
        } catch (Exception ex) {
            report.warn("data/fusions.xml", ex.getMessage());
        }
        try {
            new EnemyXmlParser().parseInto(loader.loadRoot("data/enemies.xml"), m, report);
        } catch (Exception ex) {
            report.warn("data/enemies.xml", ex.getMessage());
        }

        return m.freeze();
    }

    private LevelRepository loadLevels(DataValidationReport report, LevelRepository fallback) {
        LevelRepository.MutableLevels m = new LevelRepository.MutableLevels();

        // Start from fallback so partial XML can override.
        for (int lvl = 1; lvl <= 50; lvl++) {
            LevelDefinition def = fallback.getLevel(lvl).orElse(null);
            if (def != null) m.levels.put(lvl, def);
        }
        try {
            new LevelXmlParser().parseInto(loader.loadRoot("data/levels.xml"), m, report);
        } catch (Exception ex) {
            report.warn("data/levels.xml", ex.getMessage());
        }
        return m.freeze();
    }

    private BalanceRepository loadEconomy(DataValidationReport report, BalanceRepository fallback) {
        try {
            return new EconomyXmlParser().parse(loader.loadRoot("data/economy.xml"), report, fallback);
        } catch (Exception ex) {
            report.warn("data/economy.xml", ex.getMessage());
            return fallback;
        }
    }

    private UnlockRepository loadUnlocks(DataValidationReport report, UnlockRepository fallback) {
        try {
            return new UnlockXmlParser().parse(loader.loadRoot("data/unlocks.xml"), report, fallback);
        } catch (Exception ex) {
            report.warn("data/unlocks.xml", ex.getMessage());
            return fallback;
        }
    }

    private TutorialRepository loadTutorial(DataValidationReport report, TutorialRepository fallback) {
        try {
            return new TutorialXmlParser().parse(loader.loadRoot("data/tutorial.xml"), report, fallback);
        } catch (Exception ex) {
            report.warn("data/tutorial.xml", ex.getMessage());
            return fallback;
        }
    }

    private LocalizationRepository loadLocalization(DataValidationReport report, LocalizationRepository fallback) {
        try {
            return new LocalizationXmlParser().parse(loader.loadRoot("data/localization_en.xml"), report, fallback);
        } catch (Exception ex) {
            report.warn("data/localization_en.xml", ex.getMessage());
            return fallback;
        }
    }

    private void loadAdsRewards(DataValidationReport report) {
        try {
            new AdsRewardXmlParser().parse(loader.loadRoot("data/ads_rewards.xml"), report);
        } catch (Exception ex) {
            report.warn("data/ads_rewards.xml", ex.getMessage());
        }
    }

    private void logReport(LoadedData data) {
        DataValidationReport report = data.report;
        int errors = 0;
        int warnings = 0;
        for (var i : report.getIssues()) {
            switch (i.severity()) {
                case ERROR -> errors++;
                case WARNING -> warnings++;
            }
        }
        Gdx.app.log(AppConstants.LOG_TAG, "Design data validation: " + errors + " errors, " + warnings + " warnings");
        int printed = 0;
        for (var i : report.getIssues()) {
            if (printed >= 20) break;
            if (i.severity() == DataValidationReport.Severity.INFO) continue;
            String line = i.severity() + " [" + i.source() + "] " + i.message();
            if (i.severity() == DataValidationReport.Severity.ERROR) {
                Gdx.app.error(AppConstants.LOG_TAG, line);
            } else {
                Gdx.app.log(AppConstants.LOG_TAG, line);
            }
            printed++;
        }

        Gdx.app.log(AppConstants.LOG_TAG, "SPLICE LAB DATA LOAD REPORT");
        Gdx.app.log(AppConstants.LOG_TAG, "XML loaded: true (with fallbacks per-file)");
        Gdx.app.log(AppConstants.LOG_TAG, "Entities: " + countEntities(data.definitions));
        Gdx.app.log(AppConstants.LOG_TAG, "Items: " + countItems(data.definitions));
        Gdx.app.log(AppConstants.LOG_TAG, "Fusions: " + countFusions(data.definitions));
        Gdx.app.log(AppConstants.LOG_TAG, "Enemies: " + countEnemies(data.definitions));
        Gdx.app.log(AppConstants.LOG_TAG, "Levels: " + countLevels(data.levels));
        Gdx.app.log(AppConstants.LOG_TAG, "Localization keys: " + countLocalizationKeys(data.localization));
        // unlock/tutorial/ads counts are logged by parsers; localization count by parser.
    }

    private int countLocalizationKeys(LocalizationRepository repo) {
        // No public iteration API; parse count via known access pattern.
        // For now, rely on the fact that LocalizationRepository stores everything in a private map.
        // We'll add a size() accessor instead.
        return repo.size();
    }

    private int countEntities(DefinitionRepository repo) {
        int c = 0;
        for (var e : com.splicelab.model.EntityType.values()) if (repo.getEntity(e).isPresent()) c++;
        return c;
    }

    private int countItems(DefinitionRepository repo) {
        int c = 0;
        for (var i : com.splicelab.model.ItemType.values()) if (repo.getItem(i).isPresent()) c++;
        return c;
    }

    private int countEnemies(DefinitionRepository repo) {
        int c = 0;
        for (var e : com.splicelab.model.enemy.EnemyType.values()) if (repo.getEnemy(e).isPresent()) c++;
        return c;
    }

    private int countFusions(DefinitionRepository repo) {
        int c = 0;
        for (var e : com.splicelab.model.EntityType.values()) {
            for (var i : com.splicelab.model.ItemType.values()) {
                if (repo.getFusion(e, i).isPresent()) c++;
            }
        }
        return c;
    }

    private int countLevels(LevelRepository repo) {
        int c = 0;
        for (int i = 1; i <= 50; i++) if (repo.getLevel(i).isPresent()) c++;
        return c;
    }

    private static DefinitionRepository.MutableDefinitions extractMutable(DefinitionRepository repo) {
        // Current DefinitionRepository has no iteration API; reuse starter fallback by re-creating.
        // For now, caller passes fallback and we rebuild from it directly.
        // This method keeps extension point for future.
        DefinitionRepository.MutableDefinitions m = new DefinitionRepository.MutableDefinitions();
        DefinitionRepository starter = DefinitionRepository.createStarter();
        for (var e : com.splicelab.model.EntityType.values()) starter.getEntity(e).ifPresent(d -> m.entities.put(e, d));
        for (var i : com.splicelab.model.ItemType.values()) starter.getItem(i).ifPresent(d -> m.items.put(i, d));
        for (var e : com.splicelab.model.enemy.EnemyType.values()) starter.getEnemy(e).ifPresent(d -> m.enemies.put(e, d));
        for (var e : com.splicelab.model.EntityType.values())
            for (var i : com.splicelab.model.ItemType.values())
                starter.getFusion(e, i).ifPresent(d -> m.fusions.put(e.name() + "+" + i.name(), d));
        return m;
    }
}
