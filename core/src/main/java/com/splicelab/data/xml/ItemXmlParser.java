package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.model.AttackElement;
import com.splicelab.model.ItemType;
import com.splicelab.model.ingredient.ItemDefinition;
import com.splicelab.model.stats.StatBlock;

public final class ItemXmlParser {
    public void parseInto(XmlReader.Element root, DefinitionRepository.MutableDefinitions out, DataValidationReport report) {
        if (root == null) {
            report.error("items.xml", "Root is null");
            return;
        }

        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element el = root.getChild(i);
            if (!"item".equals(el.getName())) continue;

            String idRaw = el.getAttribute("id", "").trim();
            ItemType id = safeEnum(ItemType.class, idRaw);
            if (id == null) {
                report.error("items.xml", "Invalid item id: " + idRaw);
                continue;
            }

            String displayNameKey = el.getAttribute("displayNameKey", "");
            String shortKey = el.getAttribute("shortDescriptionKey", "");
            String longKey = el.getAttribute("longDescriptionKey", "");
            String role = el.getAttribute("role", "");
            int hpMod = parseInt(el, "hpMod", 0, report, "items.xml", "item " + id);
            int atkMod = parseInt(el, "atkMod", 0, report, "items.xml", "item " + id);
            float intervalMod = parseFloat(el, "attackIntervalModSeconds", 0f, report, "items.xml", "item " + id);
            float specialBonus = parseFloat(el, "specialChanceMod", 0f, report, "items.xml", "item " + id);
            float varianceMod = parseFloat(el, "varianceMod", 0f, report, "items.xml", "item " + id);
            AttackElement element = safeEnum(AttackElement.class, el.getAttribute("element", "NEUTRAL"));
            if (element == null) element = AttackElement.NEUTRAL;

            String projectileModKey = el.getAttribute("projectileModifierKey", "");
            String visualKey = el.getAttribute("visualKey", "");
            boolean unlockDefault = el.getBooleanAttribute("unlockDefault", false);

            ItemDefinition def = new ItemDefinition(
                    id,
                    id.name(),
                    new StatBlock(hpMod, atkMod),
                    specialBonus,
                    varianceMod,
                    element,
                    intervalMod,
                    "",
                    displayNameKey,
                    shortKey,
                    longKey,
                    role,
                    projectileModKey,
                    visualKey,
                    unlockDefault
            );
            out.items.put(id, def);
            count++;
        }
        report.info("items.xml", "Loaded items: " + count);
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
