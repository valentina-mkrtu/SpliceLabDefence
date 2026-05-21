package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.model.enemy.EnemyAttackDefinition;
import com.splicelab.model.enemy.EnemyDefinition;
import com.splicelab.model.enemy.EnemyType;

public final class EnemyXmlParser {
    public void parseInto(XmlReader.Element root, DefinitionRepository.MutableDefinitions out, DataValidationReport report) {
        if (root == null) {
            report.error("enemies.xml", "Root is null");
            return;
        }

        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element el = root.getChild(i);
            if (!"enemy".equals(el.getName())) continue;

            String idRaw = el.getAttribute("id", "").trim();
            EnemyType id = safeEnum(EnemyType.class, idRaw);
            if (id == null) {
                report.error("enemies.xml", "Invalid enemy id: " + idRaw);
                continue;
            }

            String displayNameKey = el.getAttribute("displayNameKey", "");
            String shortKey = el.getAttribute("shortDescriptionKey", "");
            int hp = parseInt(el, "baseHp", 1, report, "enemies.xml", "enemy " + id);
            int atk = parseInt(el, "baseAtk", 1, report, "enemies.xml", "enemy " + id);
            float interval = parseFloat(el, "attackIntervalSeconds", 1f, report, "enemies.xml", "enemy " + id);
            float rewardWeight = parseFloat(el, "rewardWeight", parseFloat(el, "threat", 1f, report, "enemies.xml", "enemy " + id), report, "enemies.xml", "enemy " + id);
            String visualKey = el.getAttribute("visualKey", el.getAttribute("silhouette", ""));
            String projectileKey = el.getAttribute("attackProjectileKey", "");

            EnemyDefinition def = new EnemyDefinition(
                    id,
                    id.name(),
                    hp,
                    new EnemyAttackDefinition(atk, interval),
                    projectileKey,
                    rewardWeight
            );
            out.enemies.put(id, def);
            count++;
        }
        report.info("enemies.xml", "Loaded enemies: " + count);
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
