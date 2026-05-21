package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;

public final class AdsRewardXmlParser {
    public void parse(XmlReader.Element root, DataValidationReport report) {
        if (root == null) {
            report.error("ads_rewards.xml", "Root is null");
            return;
        }
        int count = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            if ("placement".equals(root.getChild(i).getName())) count++;
        }
        report.info("ads_rewards.xml", "Loaded ad placements: " + count);
    }
}
