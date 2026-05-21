package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.LocalizationRepository;

public final class LocalizationXmlParser {
    public LocalizationRepository parse(XmlReader.Element root, DataValidationReport report, LocalizationRepository fallback) {
        if (root == null) {
            report.error("localization_en.xml", "Root is null");
            return fallback;
        }
        LocalizationRepository repo = new LocalizationRepository();
        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element el = root.getChild(i);
            if (!"string".equals(el.getName())) continue;
            String key = el.getAttribute("key", "");
            String value = el.getAttribute("value", el.getText());
            if (key == null || key.isBlank()) continue;
            repo.put(key, value);
            count++;
        }
        report.info("localization_en.xml", "Loaded localization keys: " + count);
        return repo;
    }
}
