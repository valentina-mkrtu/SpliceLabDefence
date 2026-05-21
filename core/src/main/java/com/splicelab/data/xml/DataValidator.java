package com.splicelab.data.xml;

import com.splicelab.data.BalanceRepository;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.data.LevelRepository;
import com.splicelab.data.LocalizationRepository;
import com.splicelab.data.TutorialRepository;
import com.splicelab.data.UnlockRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyType;
import com.splicelab.model.level.LevelDefinition;

import java.util.HashSet;
import java.util.Set;

public final class DataValidator {
    public DataValidationReport validate(
            DefinitionRepository definitions,
            LevelRepository levels,
            BalanceRepository balance,
            UnlockRepository unlocks,
            TutorialRepository tutorial,
            LocalizationRepository localization
    ) {
        DataValidationReport report = new DataValidationReport();

        // Minimal validation hooks now; will grow with designer XML.
        for (EntityType e : EntityType.values()) {
            if (definitions.getEntity(e).isEmpty()) {
                report.warn("definitions", "Missing entity definition: " + e);
            }
        }
        for (ItemType i : ItemType.values()) {
            if (definitions.getItem(i).isEmpty()) {
                report.warn("definitions", "Missing item definition: " + i);
            }
        }
        for (EnemyType e : EnemyType.values()) {
            if (definitions.getEnemy(e).isEmpty()) {
                report.warn("definitions", "Missing enemy definition: " + e);
            }
        }

        // Fusion coverage: every entity+item pair exists.
        int missingFusionPairs = 0;
        for (EntityType e : EntityType.values()) {
            for (ItemType i : ItemType.values()) {
                if (definitions.getFusion(e, i).isEmpty()) {
                    missingFusionPairs++;
                }
            }
        }
        if (missingFusionPairs > 0) {
            report.error("fusions", "Missing fusion pairs: " + missingFusionPairs);
        }

        LevelDefinition lvl1 = levels.getLevel(1).orElse(null);
        if (lvl1 == null) report.error("levels", "Missing level 1 definition");

        int levelCount = 0;
        Set<Integer> seen = new HashSet<>();
        for (int i = 1; i <= 50; i++) {
            if (levels.getLevel(i).isPresent()) {
                levelCount++;
                seen.add(i);
            }
        }
        if (levelCount < 50) {
            report.warn("levels", "Expected 50 levels, found " + levelCount);
        }

        // Reference validation for levels.
        for (int i = 1; i <= 50; i++) {
            LevelDefinition lvl = levels.getLevel(i).orElse(null);
            if (lvl == null) continue;
            if (lvl.availableEntities.isEmpty()) report.error("levels", "Level " + i + " has no allowed entities");
            if (lvl.availableItems.isEmpty()) report.error("levels", "Level " + i + " has no allowed items");
            if (lvl.enemyPool.isEmpty()) report.error("levels", "Level " + i + " has no enemies in pool");
            for (EntityType e : lvl.availableEntities) if (definitions.getEntity(e).isEmpty()) report.error("levels", "Level " + i + " references missing entity " + e);
            for (ItemType it : lvl.availableItems) if (definitions.getItem(it).isEmpty()) report.error("levels", "Level " + i + " references missing item " + it);
            for (EnemyType en : lvl.enemyPool) if (definitions.getEnemy(en).isEmpty()) report.error("levels", "Level " + i + " references missing enemy " + en);
            if (lvl.durationSeconds <= 0) report.error("levels", "Level " + i + " durationSeconds must be >0");
            if (lvl.tubeHp <= 0) report.error("levels", "Level " + i + " tubeHp must be >0");
        }

        // Sanity check unlock slot ranges.
        if (unlocks.getMaxSlotsPerSide() <= 0) {
            report.error("unlocks", "maxSlotsPerSide must be > 0");
        }

        // Localization: warn if any referenced keys are missing.
        for (EntityType e : EntityType.values()) {
            definitions.getEntity(e).ifPresent(def -> {
                if (!def.displayNameKey.isEmpty() && localization.get(def.displayNameKey, null).equals(def.displayNameKey)) {
                    report.warn("localization", "Missing key: " + def.displayNameKey);
                }
            });
        }

        return report;
    }
}
