package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.app.GameConfig;

public final class GameConfigXmlParser {
    public GameConfig parse(XmlReader.Element root, DataValidationReport report, GameConfig fallback) {
        if (root == null) {
            report.error("game_config.xml", "Root is null");
            return fallback;
        }
        // Current schema is minimal; keep fallback for fields not present.
        int maxLeft = getInt(root, "conveyor/maxLeftSlots@value", fallback.maxConveyorSlotsPerSide, report);
        int maxRight = getInt(root, "conveyor/maxRightSlots@value", fallback.maxConveyorSlotsPerSide, report);
        int maxSide = Math.max(maxLeft, maxRight);
        report.info("game_config.xml", "Loaded config: maxLeftSlots=" + maxLeft + ", maxRightSlots=" + maxRight);
        return new GameConfig(fallback.saveSchemaVersion, maxSide);
    }

    private static int getInt(XmlReader.Element root, String path, int fallback, DataValidationReport report) {
        try {
            String[] parts = path.split("/");
            XmlReader.Element cur = root;
            for (String p : parts) {
                if (p.contains("@")) {
                    String[] a = p.split("@");
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
}
