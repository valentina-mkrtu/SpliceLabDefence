package com.splicelab.data.xml;

import com.badlogic.gdx.utils.XmlReader;
import com.splicelab.data.BalanceRepository;

public final class EconomyXmlParser {
    public BalanceRepository parse(XmlReader.Element root, DataValidationReport report, BalanceRepository fallback) {
        if (root == null) {
            report.error("economy.xml", "Root is null");
            return fallback;
        }
        int currencyCount = 0;
        XmlReader.Element currencies = root.getChildByName("currencies");
        if (currencies != null) {
            for (int i = 0; i < currencies.getChildCount(); i++) {
                if ("currency".equals(currencies.getChild(i).getName())) currencyCount++;
            }
        }
        int boosts = 0;
        XmlReader.Element dnaBoosts = root.getChildByName("dnaBoosts");
        if (dnaBoosts != null) {
            for (int i = 0; i < dnaBoosts.getChildCount(); i++) {
                if ("boost".equals(dnaBoosts.getChild(i).getName())) boosts++;
            }
        }
        report.info("economy.xml", "Loaded currencies=" + currencyCount + ", dnaBoosts=" + boosts);
        return fallback;
    }
}
