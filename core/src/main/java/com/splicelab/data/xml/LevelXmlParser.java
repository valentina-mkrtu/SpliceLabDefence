package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.LevelRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyType;
import com.splicelab.model.level.LevelDefinition;
import com.splicelab.model.level.LevelRewardDefinition;

import java.util.ArrayList;
import java.util.List;

public final class LevelXmlParser {
    public void parseInto(XmlReader.Element root, LevelRepository.MutableLevels out, DataValidationReport report) {
        if (root == null) {
            report.error("levels.xml", "Root is null");
            return;
        }

        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element levelEl = root.getChild(i);
            if (!"level".equals(levelEl.getName())) continue;

            int number = parseInt(levelEl, "number", -1, report, "levels.xml", "level");
            if (number <= 0) {
                report.error("levels.xml", "Level missing/invalid number");
                continue;
            }

            float duration = parseFloat(levelEl, "durationSeconds", 180f, report, "levels.xml", "level " + number);
            float tubeCooldownSeconds = parseFloat(levelEl, "tubeCooldownSeconds", -1f, report, "levels.xml", "level " + number);
            int maxTubeCharges = parseInt(levelEl, "maxTubeCharges", -1, report, "levels.xml", "level " + number);
            float hpMult = parseFloat(levelEl, "enemyHpMultiplier", 1f, report, "levels.xml", "level " + number);
            float atkMult = parseFloat(levelEl, "enemyAtkMultiplier", 1f, report, "levels.xml", "level " + number);
            float spawnInterval = parseFloat(levelEl, "enemySpawnIntervalSeconds", 2.2f, report, "levels.xml", "level " + number);

            int unlockedLeft = parseInt(levelEl, "unlockedConveyorSlotsLeft", 1, report, "levels.xml", "level " + number);
            int unlockedRight = parseInt(levelEl, "unlockedConveyorSlotsRight", 1, report, "levels.xml", "level " + number);
            int tubeHp = parseInt(levelEl, "tubeHp", 300, report, "levels.xml", "level " + number);
            String tutorialStepId = levelEl.getAttribute("tutorialStepId", null);
            String themeTag = levelEl.getAttribute("levelThemeTag", "");
            float difficulty = parseFloat(levelEl, "difficultyRating", 1f, report, "levels.xml", "level " + number);

            List<EntityType> entities = parseAllowedEntities(levelEl, report, number);
            List<ItemType> items = parseAllowedItems(levelEl, report, number);
            List<LevelDefinition.EnemySpawnEntry> enemyPool = parseEnemyPool(levelEl, report, number);

            XmlReader.Element rewardsEl = levelEl.getChildByName("rewards");
            int winCoins = rewardsEl == null ? parseInt(levelEl, "baseCoins", 0, report, "levels.xml", "level " + number) : parseInt(rewardsEl, "winCoins", 0, report, "levels.xml", "level " + number);
            int winDna = rewardsEl == null ? parseInt(levelEl, "baseDna", 0, report, "levels.xml", "level " + number) : parseInt(rewardsEl, "winDna", 0, report, "levels.xml", "level " + number);
            int firstBonusCoins = rewardsEl == null ? 0 : parseInt(rewardsEl, "firstWinBonusCoins", 0, report, "levels.xml", "level " + number);
            int firstBonusDna = rewardsEl == null ? 0 : parseInt(rewardsEl, "firstWinBonusDna", 0, report, "levels.xml", "level " + number);
            LevelRewardDefinition rewards = new LevelRewardDefinition(winCoins, winDna, firstBonusCoins, firstBonusDna);

            LevelDefinition def = new LevelDefinition(
                    number,
                    duration,
                    tubeHp,
                    tubeCooldownSeconds,
                    maxTubeCharges,
                    unlockedLeft,
                    unlockedRight,
                    entities,
                    items,
                    enemyPool,
                    hpMult,
                    atkMult,
                    spawnInterval,
                    rewards,
                    levelEl.getAttribute("storyKey", ""),
                    tutorialStepId
            );
            out.levels.put(number, def);
            count++;
        }

        report.info("levels.xml", "Loaded levels: " + count);
    }

    private static List<EntityType> parseAllowedEntities(XmlReader.Element levelEl, DataValidationReport report, int number) {
        List<EntityType> list = new ArrayList<>();
        XmlReader.Element allowed = levelEl.getChildByName("allowedEntities");
        if (allowed == null) return list;
        for (int i = 0; i < allowed.getChildCount(); i++) {
            XmlReader.Element e = allowed.getChild(i);
            if (!"entity".equals(e.getName())) continue;
            EntityType type = safeEnum(EntityType.class, e.getAttribute("id", ""));
            if (type == null) {
                report.warn("levels.xml", "Level " + number + " has invalid entity id");
            } else {
                list.add(type);
            }
        }
        return list;
    }

    private static List<ItemType> parseAllowedItems(XmlReader.Element levelEl, DataValidationReport report, int number) {
        List<ItemType> list = new ArrayList<>();
        XmlReader.Element allowed = levelEl.getChildByName("allowedItems");
        if (allowed == null) return list;
        for (int i = 0; i < allowed.getChildCount(); i++) {
            XmlReader.Element e = allowed.getChild(i);
            if (!"item".equals(e.getName())) continue;
            ItemType type = safeEnum(ItemType.class, e.getAttribute("id", ""));
            if (type == null) {
                report.warn("levels.xml", "Level " + number + " has invalid item id");
            } else {
                list.add(type);
            }
        }
        return list;
    }

    private static List<LevelDefinition.EnemySpawnEntry> parseEnemyPool(XmlReader.Element levelEl, DataValidationReport report, int number) {
        List<LevelDefinition.EnemySpawnEntry> list = new ArrayList<>();
        XmlReader.Element pool = levelEl.getChildByName("enemyPool");
        if (pool == null) return list;
        for (int i = 0; i < pool.getChildCount(); i++) {
            XmlReader.Element e = pool.getChild(i);
            if (!"enemy".equals(e.getName())) continue;
            EnemyType type = safeEnum(EnemyType.class, e.getAttribute("id", ""));
            if (type == null) {
                report.warn("levels.xml", "Level " + number + " has invalid enemy id");
            } else {
                float weight = 1f;
                String wRaw = e.getAttribute("weight", "");
                if (wRaw != null && !wRaw.isBlank()) {
                    try {
                        weight = Float.parseFloat(wRaw.trim());
                    } catch (Exception ex) {
                        report.warn("levels.xml", "Level " + number + " has invalid enemy weight: " + wRaw);
                        weight = 1f;
                    }
                }
                list.add(new LevelDefinition.EnemySpawnEntry(type, weight));
            }
        }
        return list;
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> clz, String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;
        try {
            return Enum.valueOf(clz, v);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int parseInt(XmlReader.Element el, String attr, int fallback, DataValidationReport report, String src, String ctx) {
        String raw = el.getAttribute(attr, "");
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ex) {
            report.warn(src, "Bad int " + attr + " for " + ctx + ": " + raw);
            return fallback;
        }
    }

    private static float parseFloat(XmlReader.Element el, String attr, float fallback, DataValidationReport report, String src, String ctx) {
        String raw = el.getAttribute(attr, "");
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Float.parseFloat(raw.trim());
        } catch (Exception ex) {
            report.warn(src, "Bad float " + attr + " for " + ctx + ": " + raw);
            return fallback;
        }
    }
}
