package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.DefinitionRepository;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.ingredient.FusionDefinition;

public final class FusionXmlParser {
    public void parseInto(XmlReader.Element root, DefinitionRepository.MutableDefinitions out, DataValidationReport report) {
        if (root == null) {
            report.error("fusions.xml", "Root is null");
            return;
        }

        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element el = root.getChild(i);
            if (!"fusion".equals(el.getName())) continue;

            String id = el.getAttribute("id", "").trim();
            if (id.isEmpty()) {
                report.error("fusions.xml", "Fusion missing id");
                continue;
            }
            String entityRaw = el.getAttribute("entityId", el.getAttribute("entity", "")).trim();
            String itemRaw = el.getAttribute("itemId", el.getAttribute("item", "")).trim();
            EntityType entity = safeEnum(EntityType.class, entityRaw);
            ItemType item = safeEnum(ItemType.class, itemRaw);
            if (entity == null || item == null) {
                report.error("fusions.xml", "Fusion " + id + " invalid entity/item: " + entityRaw + "/" + itemRaw);
                continue;
            }

            String displayNameKey = el.getAttribute("displayNameKey", "");
            String shortKey = el.getAttribute("shortDescriptionKey", "");
            String abilityNameKey = el.getAttribute("abilityNameKey", "");
            String abilityDescKey = el.getAttribute("abilityDescriptionKey", "");
            String projectileKey = el.getAttribute("projectileKey", "");
            String auraKey = el.getAttribute("auraKey", "");
            String element = el.getAttribute("element", "");
            String specialBehavior = el.getAttribute("specialBehavior", "");
            String tags = el.getAttribute("tags", "");
            String silhouette = el.getAttribute("silhouette", "");

            FusionDefinition def = new FusionDefinition(
                    id,
                    entity,
                    item,
                    id,
                    projectileKey,
                    auraKey,
                    element,
                    specialBehavior,
                    tags,
                    displayNameKey,
                    shortKey,
                    abilityNameKey,
                    abilityDescKey,
                    silhouette
            );
            out.fusions.put(entity.name() + "+" + item.name(), def);
            count++;
        }
        report.info("fusions.xml", "Loaded fusions: " + count);
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
}
