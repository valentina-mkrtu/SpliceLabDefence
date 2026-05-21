package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.UnlockRepository;

public final class UnlockXmlParser {
    public UnlockRepository parse(XmlReader.Element root, DataValidationReport report, UnlockRepository fallback) {
        if (root == null) {
            report.error("unlocks.xml", "Root is null");
            return fallback;
        }
        // Current unlocks.xml defines milestones and unlock entries, but we only
        // need maxSlotsPerSide for now (computed from config). Keep parser for counts.
        int milestones = 0;
        int unlocks = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element m = root.getChild(i);
            if (!"milestone".equals(m.getName())) continue;
            milestones++;
            unlocks += m.getChildCount();
        }
        report.info("unlocks.xml", "Loaded milestones=" + milestones + ", unlock entries=" + unlocks);
        return fallback;
    }
}
