package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.app.GameConfig;

public final class GameConfigXmlParser {
    public GameConfig parse(XmlReader.Element root, DataValidationReport report, GameConfig fallback) {
        if (root == null) {
            report.error("game_config.xml", "Root is null");
            return fallback;
        }
        int tubeMaxHp = getInt(root, "tube@maxHp", fallback.tubeMaxHp, report);

        int gridCols = getInt(root, "grid@columns", fallback.gridCols, report);
        int gridRows = getInt(root, "grid@rows", fallback.gridRows, report);
        int gridTotalSlots = getInt(root, "grid@totalSlots", fallback.gridTotalSlots, report);

        float tubeTapCooldown = getFloat(root, "spawn/tubeTapCooldownSeconds@value", fallback.tubeCooldownSeconds, report);
        float entityWeight = getFloat(root, "spawn/dropRates@entityWeight", fallback.spawnEntityWeight, report);
        float itemWeight = getFloat(root, "spawn/dropRates@itemWeight", fallback.spawnItemWeight, report);
        int pityEveryX = getInt(root, "spawn/pitySystem@guaranteeEntityEveryXSpawns", fallback.pityGuaranteeEntityEveryXItemSpawns, report);
        int perTap = getInt(root, "spawn/spawnPerTap@value", fallback.tubeSpawnPerTap, report);

        int maxLeft = getInt(root, "conveyor/maxLeftSlots@value", fallback.maxConveyorSlotsPerSide, report);
        int maxRight = getInt(root, "conveyor/maxRightSlots@value", fallback.maxConveyorSlotsPerSide, report);
        int maxSide = Math.max(maxLeft, maxRight);

        float defeatCoinsMult = getFloat(root, "combat/defeatRewardMultiplier@coins", fallback.defeatCoinsMultiplier, report);
        float defeatDnaMult = getFloat(root, "combat/defeatRewardMultiplier@dna", fallback.defeatDnaMultiplier, report);

        report.info(
                "game_config.xml",
                "Loaded config: tubeMaxHp=" + tubeMaxHp
                        + ", grid=" + gridCols + "x" + gridRows + " totalSlots=" + gridTotalSlots
                        + ", tubeTapCooldownSeconds=" + tubeTapCooldown
                        + ", spawnPerTap=" + perTap
                        + ", dropRates(entity=" + entityWeight + ", item=" + itemWeight + ")"
                        + ", pityEveryX=" + pityEveryX
                        + ", maxLeftSlots=" + maxLeft + ", maxRightSlots=" + maxRight
        );

        return new GameConfig(
                fallback.saveSchemaVersion,
                maxSide,
                tubeTapCooldown,
                fallback.maxTubeCharges,
                tubeMaxHp,
                gridCols,
                gridRows,
                gridTotalSlots,
                perTap,
                entityWeight,
                itemWeight,
                pityEveryX
                ,
                defeatCoinsMult,
                defeatDnaMult
        );
    }

    private static int getInt(XmlReader.Element root, String path, int fallback, DataValidationReport report) {
        try {
            String[] parts = path.split("/");
            XmlReader.Element cur = root;
            for (String p : parts) {
                if (p.contains("@")) {
                    String[] a = p.split("@");
                    if (a[0].isEmpty()) {
                        return Integer.parseInt(cur.getAttribute(a[1], String.valueOf(fallback)));
                    }
                    return Integer.parseInt(cur.getChildByName(a[0]).getAttribute(a[1], String.valueOf(fallback)));
                }
                cur = cur.getChildByName(p);
                if (cur == null) return fallback;
            }
            return fallback;
        } catch (Exception ex) {
            report.warn("game_config.xml", "Bad int for " + path);
            return fallback;
        }
    }

    private static float getFloat(XmlReader.Element root, String path, float fallback, DataValidationReport report) {
        try {
            String[] parts = path.split("/");
            XmlReader.Element cur = root;
            for (String p : parts) {
                if (p.contains("@")) {
                    String[] a = p.split("@");
                    if (a[0].isEmpty()) {
                        return Float.parseFloat(cur.getAttribute(a[1], String.valueOf(fallback)));
                    }
                    return Float.parseFloat(cur.getChildByName(a[0]).getAttribute(a[1], String.valueOf(fallback)));
                }
                cur = cur.getChildByName(p);
                if (cur == null) return fallback;
            }
            return fallback;
        } catch (Exception ex) {
            report.warn("game_config.xml", "Bad float for " + path);
            return fallback;
        }
    }
}
