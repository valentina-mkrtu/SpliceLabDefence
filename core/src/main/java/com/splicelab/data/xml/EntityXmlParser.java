package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.model.AttackElement;
import com.splicelab.model.EntityType;
import com.splicelab.model.ingredient.EntityDefinition;
import com.splicelab.model.stats.CombatStats;
import com.splicelab.model.stats.SpecialAttackData;

public final class EntityXmlParser {
    public void parseInto(XmlReader.Element root, DefinitionRepository.MutableDefinitions out, DataValidationReport report) {
        if (root == null) {
            report.error("entities.xml", "Root is null");
            return;
        }

        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element el = root.getChild(i);
            if (!"entity".equals(el.getName())) continue;
            String idRaw = el.getAttribute("id", "").trim();
            EntityType id = safeEnum(EntityType.class, idRaw);
            if (id == null) {
                report.error("entities.xml", "Invalid entity id: " + idRaw);
                continue;
            }

            String displayNameKey = el.getAttribute("displayNameKey", "");
            String shortKey = el.getAttribute("shortDescriptionKey", "");
            String longKey = el.getAttribute("longDescriptionKey", "");
            String role = el.getAttribute("role", "");
            int hp = parseInt(el, "baseHp", 1, report, "entities.xml", "entity " + id);
            int atk = parseInt(el, "baseAtk", 0, report, "entities.xml", "entity " + id);
            float interval = parseFloat(el, "attackIntervalSeconds", 1f, report, "entities.xml", "entity " + id);
            float specialChance = clamp01(parseFloat(el, "specialChance", 0f, report, "entities.xml", "entity " + id));
            float variance = clamp(parseFloat(el, "variance", 0f, report, "entities.xml", "entity " + id), 0f, 2f);
            AttackElement element = safeEnum(AttackElement.class, el.getAttribute("element", "NEUTRAL"));
            if (element == null) element = AttackElement.NEUTRAL;

            // Optional fields (present in task spec but not currently in XML)
            String projectileKey = el.getAttribute("projectileKey", "");
            String visualKey = el.getAttribute("visualKey", el.getAttribute("silhouette", ""));
            String specialAttackId = el.getAttribute("specialAttackId", "");
            boolean unlockDefault = el.getBooleanAttribute("unlockDefault", false);

            EntityDefinition def = new EntityDefinition(
                    id,
                    id.name(),
                    new CombatStats(hp, atk, interval, specialChance, variance),
                    new SpecialAttackData(specialAttackId, ""),
                    element,
                    "",
                    displayNameKey,
                    shortKey,
                    longKey,
                    role,
                    projectileKey,
                    visualKey,
                    specialAttackId,
                    unlockDefault
            );
            out.entities.put(id, def);
            count++;
        }
        report.info("entities.xml", "Loaded entities: " + count);
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

    private static float clamp01(float v) {
        return clamp(v, 0f, 1f);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
