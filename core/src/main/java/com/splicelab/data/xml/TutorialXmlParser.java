package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.TutorialRepository;

public final class TutorialXmlParser {
    public TutorialRepository parse(XmlReader.Element root, DataValidationReport report, TutorialRepository fallback) {
        if (root == null) {
            report.error("tutorial.xml", "Root is null");
            return fallback;
        }
        int steps = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            if ("step".equals(root.getChild(i).getName())) steps++;
        }
        report.info("tutorial.xml", "Loaded tutorial steps: " + steps);
        return fallback;
    }
}
