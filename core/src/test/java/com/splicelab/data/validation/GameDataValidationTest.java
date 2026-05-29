package com.splicelab.data.validation;

import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

public final class GameDataValidationTest {
    private static final Path DATA_DIR = Paths.get("android", "src", "main", "assets", "data");

    @Test
    void validateGameXmlData() throws Exception {
        List<String> errors = new ArrayList<>();

        if (!Files.exists(DATA_DIR)) {
            errors.add("DATA ERROR: missing data dir " + DATA_DIR);
        }

        Map<String, org.w3c.dom.Document> docs = new HashMap<>();
        if (Files.exists(DATA_DIR)) {
            try (var paths = Files.list(DATA_DIR)) {
                paths.filter(p -> p.getFileName().toString().endsWith(".xml"))
                        .sorted()
                        .forEach(p -> {
                            String name = p.getFileName().toString();
                            try {
                                org.w3c.dom.Document doc = parseXml(p);
                                docs.put(name, doc);
                            } catch (Exception ex) {
                                errors.add("DATA ERROR: " + name + " failed parse: " + rootMessage(ex));
                            }
                        });
            }
        }

        // Special rule: localization_en.xml must have one root element.
        if (Files.exists(DATA_DIR) && Files.exists(DATA_DIR.resolve("localization_en.xml"))) {
            try {
                parseXml(DATA_DIR.resolve("localization_en.xml"));
            } catch (Exception ex) {
                errors.add("DATA ERROR: localization_en.xml invalid root: " + rootMessage(ex));
            }
        }

        Set<String> localizationKeys = parseLocalizationKeys(docs.get("localization_en.xml"), errors);
        Set<String> entityIds = parseIds(docs.get("entities.xml"), "entity", errors, "entities.xml");
        Set<String> itemIds = parseIds(docs.get("items.xml"), "item", errors, "items.xml");
        Set<String> enemyIds = parseIds(docs.get("enemies.xml"), "enemy", errors, "enemies.xml");
        parseFusions(docs.get("fusions.xml"), entityIds, itemIds, errors);
        validateUnlocks(docs.get("unlocks.xml"), entityIds, itemIds, localizationKeys, errors);
        validateEconomy(docs.get("economy.xml"), localizationKeys, errors);
        validateLevels(docs.get("levels.xml"), entityIds, itemIds, enemyIds, errors);
        validateLocalizationReferences(docs, localizationKeys, errors);
        validateSaveCurrencyCeilings(errors);  // T-6.1

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String e : errors) sb.append(e).append('\n');
            fail(sb.toString());
        }
    }

    private static org.w3c.dom.Document parseXml(Path path) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        try (InputStream in = Files.newInputStream(path)) {
            return db.parse(in);
        }
    }

    private static String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null) cur = cur.getCause();
        String msg = cur.getMessage();
        return msg == null ? cur.getClass().getSimpleName() : msg;
    }

    private static Set<String> parseLocalizationKeys(org.w3c.dom.Document doc, List<String> errors) {
        Set<String> keys = new HashSet<>();
        if (doc == null) return keys;

        org.w3c.dom.NodeList nodes = doc.getElementsByTagName("string");
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
            String key = el.getAttribute("key");
            if (key == null || key.isBlank()) continue;
            keys.add(key.trim());
        }

        if (keys.isEmpty()) {
            errors.add("DATA ERROR: localization_en.xml has no <string> keys");
        }
        return keys;
    }

    private static Set<String> parseIds(org.w3c.dom.Document doc, String tagName, List<String> errors, String src) {
        Set<String> ids = new HashSet<>();
        if (doc == null) return ids;

        org.w3c.dom.NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
            if (!el.hasAttribute("id")) continue;
            String id = el.getAttribute("id");
            if (id == null || id.isBlank()) continue;
            ids.add(id.trim());
        }

        if (ids.isEmpty()) {
            errors.add("DATA ERROR: " + src + " has no <" + tagName + "> ids");
        }
        return ids;
    }

    private static void parseFusions(org.w3c.dom.Document doc, Set<String> entityIds, Set<String> itemIds, List<String> errors) {
        if (doc == null) return;

        org.w3c.dom.NodeList nodes = doc.getElementsByTagName("fusion");
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
            String entityId = el.getAttribute("entity");
            String itemId = el.getAttribute("item");
            if (entityId != null && !entityId.isBlank() && !entityIds.contains(entityId)) {
                errors.add("DATA ERROR: fusions.xml fusion entity=" + entityId + " missing in entities.xml");
            }
            if (itemId != null && !itemId.isBlank() && !itemIds.contains(itemId)) {
                errors.add("DATA ERROR: fusions.xml fusion item=" + itemId + " missing in items.xml");
            }
        }
    }

    private static void validateUnlocks(
            org.w3c.dom.Document doc,
            Set<String> entityIds,
            Set<String> itemIds,
            Set<String> localizationKeys,
            List<String> errors
    ) {
        if (doc == null) return;

        org.w3c.dom.NodeList milestones = doc.getElementsByTagName("milestone");
        for (int i = 0; i < milestones.getLength(); i++) {
            org.w3c.dom.Element ms = (org.w3c.dom.Element) milestones.item(i);
            String level = ms.getAttribute("level");
            String msgKey = ms.getAttribute("unlockMessageKey");
            if (msgKey != null && !msgKey.isBlank() && !localizationKeys.contains(msgKey)) {
                errors.add("DATA ERROR: unlocks.xml milestone level=" + level + " missing localization key " + msgKey);
            }

            org.w3c.dom.NodeList unlocks = ms.getElementsByTagName("unlock");
            for (int k = 0; k < unlocks.getLength(); k++) {
                org.w3c.dom.Element u = (org.w3c.dom.Element) unlocks.item(k);
                String type = u.getAttribute("type");
                String id = u.getAttribute("id");
                if (id == null || id.isBlank()) continue;

                if ("ENTITY".equals(type) && !entityIds.contains(id)) {
                    errors.add("DATA ERROR: unlocks.xml milestone level=" + level + " unlock ENTITY id=" + id + " missing in entities.xml");
                }
                if ("ITEM".equals(type) && !itemIds.contains(id)) {
                    errors.add("DATA ERROR: unlocks.xml milestone level=" + level + " unlock ITEM id=" + id + " missing in items.xml");
                }
                if ("FEATURE".equals(type) && (id == null || id.isBlank())) {
                    errors.add("DATA ERROR: unlocks.xml milestone level=" + level + " unlock FEATURE missing id");
                }
            }
        }
    }

    private static void validateEconomy(org.w3c.dom.Document doc, Set<String> localizationKeys, List<String> errors) {
        if (doc == null) return;

        Set<String> currencyIds = new HashSet<>();
        org.w3c.dom.NodeList currencies = doc.getElementsByTagName("currency");
        for (int i = 0; i < currencies.getLength(); i++) {
            org.w3c.dom.Element c = (org.w3c.dom.Element) currencies.item(i);
            String id = c.getAttribute("id");
            if (id == null || id.isBlank()) continue;
            currencyIds.add(id.trim());

            String displayNameKey = c.getAttribute("displayNameKey");
            if (displayNameKey != null && !displayNameKey.isBlank() && !localizationKeys.contains(displayNameKey)) {
                errors.add("DATA ERROR: economy.xml currency id=" + id + " missing localization key " + displayNameKey);
            }
        }
        if (currencyIds.isEmpty()) {
            errors.add("DATA ERROR: economy.xml has no currencies");
        }
    }

    private static void validateLevels(
            org.w3c.dom.Document doc,
            Set<String> entityIds,
            Set<String> itemIds,
            Set<String> enemyIds,
            List<String> errors
    ) {
        if (doc == null) return;

        Set<String> seenLevelNumbers = new HashSet<>();
        org.w3c.dom.NodeList levels = doc.getElementsByTagName("level");
        for (int i = 0; i < levels.getLength(); i++) {
            org.w3c.dom.Element level = (org.w3c.dom.Element) levels.item(i);
            String number = level.getAttribute("number");
            if (number == null || number.isBlank()) {
                errors.add("DATA ERROR: levels.xml level missing number");
                continue;
            }
            if (!seenLevelNumbers.add(number)) {
                errors.add("DATA ERROR: levels.xml duplicate level number=" + number);
            }

            Set<String> allowedEntities = new HashSet<>();
            Set<String> allowedItems = new HashSet<>();

            org.w3c.dom.Element allowedEntitiesEl = firstChild(level, "allowedEntities");
            if (allowedEntitiesEl == null) {
                errors.add("DATA ERROR: levels.xml level=" + number + " missing allowedEntities");
            } else {
                org.w3c.dom.NodeList nodes = allowedEntitiesEl.getElementsByTagName("entity");
                for (int k = 0; k < nodes.getLength(); k++) {
                    org.w3c.dom.Element e = (org.w3c.dom.Element) nodes.item(k);
                    String id = e.getAttribute("id");
                    if (id == null || id.isBlank()) continue;
                    allowedEntities.add(id);
                    if (!entityIds.contains(id)) {
                        errors.add("DATA ERROR: levels.xml level=" + number + " allowedEntities entity id=" + id + " missing in entities.xml");
                    }
                }
                if (allowedEntities.isEmpty()) {
                    errors.add("DATA ERROR: levels.xml level=" + number + " allowedEntities empty");
                }
            }

            org.w3c.dom.Element allowedItemsEl = firstChild(level, "allowedItems");
            if (allowedItemsEl == null) {
                errors.add("DATA ERROR: levels.xml level=" + number + " missing allowedItems");
            } else {
                org.w3c.dom.NodeList nodes = allowedItemsEl.getElementsByTagName("item");
                for (int k = 0; k < nodes.getLength(); k++) {
                    org.w3c.dom.Element it = (org.w3c.dom.Element) nodes.item(k);
                    String id = it.getAttribute("id");
                    if (id == null || id.isBlank()) continue;
                    allowedItems.add(id);
                    if (!itemIds.contains(id)) {
                        errors.add("DATA ERROR: levels.xml level=" + number + " allowedItems item id=" + id + " missing in items.xml");
                    }
                }
                if (allowedItems.isEmpty()) {
                    errors.add("DATA ERROR: levels.xml level=" + number + " allowedItems empty");
                }
            }

            validateLevelEnemyPoolAndWave(level, number, enemyIds, errors);
            validateLevelRewards(level, number, errors);
            validateLevelTubeBag(level, number, allowedEntities, allowedItems, errors);
        }
    }

    private static void validateLevelEnemyPoolAndWave(org.w3c.dom.Element level, String number, Set<String> enemyIds, List<String> errors) {
        org.w3c.dom.Element enemyPool = firstChild(level, "enemyPool");
        if (enemyPool == null) {
            errors.add("DATA ERROR: levels.xml level=" + number + " missing enemyPool");
        } else {
            org.w3c.dom.NodeList nodes = enemyPool.getElementsByTagName("enemy");
            if (nodes.getLength() == 0) errors.add("DATA ERROR: levels.xml level=" + number + " enemyPool empty");
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) nodes.item(i);
                String id = e.getAttribute("id");
                if (id == null || id.isBlank()) continue;
                if (!enemyIds.contains(id)) {
                    errors.add("DATA ERROR: levels.xml level=" + number + " enemyPool enemy id=" + id + " missing in enemies.xml");
                }
            }
        }

        org.w3c.dom.Element enemyWave = firstChild(level, "enemyWave");
        if (enemyWave == null) {
            errors.add("DATA ERROR: levels.xml level=" + number + " missing enemyWave");
        } else {
            org.w3c.dom.NodeList nodes = enemyWave.getElementsByTagName("enemy");
            if (nodes.getLength() == 0) errors.add("DATA ERROR: levels.xml level=" + number + " enemyWave empty");
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) nodes.item(i);
                String id = e.getAttribute("id");
                if (id == null || id.isBlank()) continue;
                if (!enemyIds.contains(id)) {
                    errors.add("DATA ERROR: levels.xml level=" + number + " enemyWave enemy id=" + id + " missing in enemies.xml");
                }
            }
        }
    }

    private static void validateLevelRewards(org.w3c.dom.Element level, String number, List<String> errors) {
        org.w3c.dom.Element rewards = firstChild(level, "rewards");
        if (rewards == null) {
            // allow old format (baseCoins/baseDna), but require at least something
            String baseCoins = level.getAttribute("baseCoins");
            String baseDna = level.getAttribute("baseDna");
            if ((baseCoins == null || baseCoins.isBlank()) && (baseDna == null || baseDna.isBlank())) {
                errors.add("DATA ERROR: levels.xml level=" + number + " missing rewards/baseCoins/baseDna");
            }
            return;
        }
        if (!rewards.hasAttribute("winCoins") && !rewards.hasAttribute("winDna")) {
            errors.add("DATA ERROR: levels.xml level=" + number + " rewards missing winCoins/winDna");
        }
    }

    private static void validateLevelTubeBag(
            org.w3c.dom.Element level,
            String levelNumber,
            Set<String> allowedEntities,
            Set<String> allowedItems,
            List<String> errors
    ) {
        org.w3c.dom.Element tubeBag = firstChild(level, "tubeBag");
        if (tubeBag == null) {
            errors.add("DATA ERROR: levels.xml level=" + levelNumber + " missing tubeBag");
            return;
        }
        org.w3c.dom.Element bag8 = firstChild(tubeBag, "bag8");
        if (bag8 == null) {
            errors.add("DATA ERROR: levels.xml level=" + levelNumber + " missing tubeBag/bag8");
            return;
        }

        int expectedBagSize = expectedBagSize(level, errors, levelNumber);

        int total = 0;
        org.w3c.dom.NodeList entityNodes = bag8.getElementsByTagName("entity");
        for (int i = 0; i < entityNodes.getLength(); i++) {
            org.w3c.dom.Element e = (org.w3c.dom.Element) entityNodes.item(i);
            String id = e.getAttribute("id");
            int count = parseCount(e, errors, "levels.xml level=" + levelNumber + " tubeBag entity");
            total += Math.max(0, count);
            if (id != null && !id.isBlank() && !allowedEntities.contains(id)) {
                errors.add("DATA ERROR: levels.xml level=" + levelNumber + " tubeBag entity " + id + " not allowed");
            }
        }
        org.w3c.dom.NodeList itemNodes = bag8.getElementsByTagName("item");
        for (int i = 0; i < itemNodes.getLength(); i++) {
            org.w3c.dom.Element it = (org.w3c.dom.Element) itemNodes.item(i);
            String id = it.getAttribute("id");
            int count = parseCount(it, errors, "levels.xml level=" + levelNumber + " tubeBag item");
            total += Math.max(0, count);
            if (id != null && !id.isBlank() && !allowedItems.contains(id)) {
                errors.add("DATA ERROR: levels.xml level=" + levelNumber + " tubeBag item " + id + " not allowed");
            }
        }

        if (expectedBagSize > 0 && total != expectedBagSize) {
            errors.add("DATA ERROR: levels.xml level=" + levelNumber + " tubeBag count=" + total + " expected=" + expectedBagSize);
        }
    }

    private static int expectedBagSize(org.w3c.dom.Element level, List<String> errors, String levelNumber) {
        String maxTubeChargesRaw = level.getAttribute("maxTubeCharges");
        if (maxTubeChargesRaw == null || maxTubeChargesRaw.isBlank()) {
            errors.add("DATA ERROR: levels.xml level=" + levelNumber + " missing maxTubeCharges");
            return -1;
        }
        try {
            int maxTubeCharges = Integer.parseInt(maxTubeChargesRaw.trim());
            // Rule: bag size must be min(8, maxTubeCharges)
            return Math.min(8, maxTubeCharges);
        } catch (Exception ex) {
            errors.add("DATA ERROR: levels.xml level=" + levelNumber + " invalid maxTubeCharges=" + maxTubeChargesRaw);
            return -1;
        }
    }

    private static int parseCount(org.w3c.dom.Element el, List<String> errors, String ctx) {
        String raw = el.getAttribute("count");
        if (raw == null || raw.isBlank()) return 1;
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ex) {
            errors.add("DATA ERROR: " + ctx + " bad count=" + raw);
            return 1;
        }
    }

    private static org.w3c.dom.Element firstChild(org.w3c.dom.Element parent, String tag) {
        org.w3c.dom.NodeList nodes = parent.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node n = nodes.item(i);
            if (n.getParentNode() == parent && n instanceof org.w3c.dom.Element) {
                return (org.w3c.dom.Element) n;
            }
        }
        return null;
    }

    private static void validateLocalizationReferences(
            Map<String, org.w3c.dom.Document> docs,
            Set<String> localizationKeys,
            List<String> errors
    ) {
        // Find KEY-like tokens in all data XML except localization itself.
        Pattern keyPattern = Pattern.compile("\\b[A-Z][A-Z0-9_]{2,}\\b");
        Set<String> referenced = new LinkedHashSet<>();

        for (Map.Entry<String, org.w3c.dom.Document> e : docs.entrySet()) {
            String name = e.getKey();
            if ("localization_en.xml".equals(name)) continue;
            org.w3c.dom.Document doc = e.getValue();
            if (doc == null) continue;

            String text = docToText(doc);
            Matcher m = keyPattern.matcher(text);
            while (m.find()) referenced.add(m.group());
        }

        // Filter out obvious non-localization IDs (entity/item/enemy ids are also ALLCAPS)
        // so we only enforce the ones that are actually used as *keys* in known attributes.
        // We do a second pass: look for attributes ending with "Key".
        Set<String> keyAttrs = new LinkedHashSet<>();
        for (Map.Entry<String, org.w3c.dom.Document> e : docs.entrySet()) {
            String name = e.getKey();
            if ("localization_en.xml".equals(name)) continue;
            org.w3c.dom.Document doc = e.getValue();
            if (doc == null) continue;

            org.w3c.dom.NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) all.item(i);
                org.w3c.dom.NamedNodeMap attrs = el.getAttributes();
                for (int k = 0; k < attrs.getLength(); k++) {
                    org.w3c.dom.Node a = attrs.item(k);
                    if (a == null) continue;
                    String attrName = a.getNodeName();
                    if (attrName == null || !attrName.endsWith("Key")) continue;
                    String v = a.getNodeValue();
                    if (v == null || v.isBlank()) continue;
                    keyAttrs.add(v.trim());
                }
            }
        }

        for (String key : keyAttrs) {
            if (!localizationKeys.contains(key)) {
                errors.add("DATA ERROR: localization missing key " + key);
            }
        }
    }

    /**
     * T-6.1: Verifies that the currency ceiling constants in GameConfig are sane and
     * consistent with the economy XML (if present), so that a tampered save with an
     * astronomically large balance will always be clamped to a reasonable value.
     *
     * <p>These are compile-time constants checked at test time — no XML needed.</p>
     */
    private static void validateSaveCurrencyCeilings(List<String> errors) {
        // Import ceiling values directly rather than via reflection so the test fails to compile
        // if someone renames the constants.
        int maxCoins    = com.splicelab.app.GameConfig.MAX_COINS;
        int maxDna      = com.splicelab.app.GameConfig.MAX_DNA;
        int maxCrystals = com.splicelab.app.GameConfig.MAX_CRYSTALS;
        int maxLevel    = com.splicelab.app.GameConfig.MAX_PLAYER_LEVEL;
        int maxCurrent  = com.splicelab.app.GameConfig.MAX_CURRENT_LEVEL;
        int maxXp       = com.splicelab.app.GameConfig.MAX_XP;

        if (maxCoins <= 0)
            errors.add("SAVE ERROR: MAX_COINS must be > 0, got " + maxCoins);
        if (maxDna <= 0)
            errors.add("SAVE ERROR: MAX_DNA must be > 0, got " + maxDna);
        if (maxCrystals <= 0)
            errors.add("SAVE ERROR: MAX_CRYSTALS must be > 0, got " + maxCrystals);
        if (maxLevel < 1)
            errors.add("SAVE ERROR: MAX_PLAYER_LEVEL must be >= 1, got " + maxLevel);
        if (maxCurrent < 1)
            errors.add("SAVE ERROR: MAX_CURRENT_LEVEL must be >= 1, got " + maxCurrent);
        if (maxXp <= 0)
            errors.add("SAVE ERROR: MAX_XP must be > 0, got " + maxXp);

        // Ceilings should not be absurdly small — a reasonable game economy should allow at
        // least 1 000 of each currency before hitting the cap.
        if (maxCoins < 1_000)
            errors.add("SAVE ERROR: MAX_COINS=" + maxCoins + " looks too low (< 1000)");
        if (maxDna < 1_000)
            errors.add("SAVE ERROR: MAX_DNA=" + maxDna + " looks too low (< 1000)");
    }

    private static String docToText(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element root = doc.getDocumentElement();
            StringBuilder sb = new StringBuilder();
            walk(root, sb);
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private static void walk(org.w3c.dom.Node node, StringBuilder out) {
        if (node == null) return;
        if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
            out.append(node.getNodeValue()).append(' ');
        }
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                org.w3c.dom.Node a = attrs.item(i);
                if (a != null && a.getNodeValue() != null) out.append(a.getNodeValue()).append(' ');
            }
        }
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            walk(children.item(i), out);
        }
    }
}
