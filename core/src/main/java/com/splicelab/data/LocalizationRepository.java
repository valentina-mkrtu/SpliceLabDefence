package com.splicelab.data;

import java.util.HashMap;
import java.util.Map;

public final class LocalizationRepository {
    private final Map<String, String> strings = new HashMap<>();

    public String get(String key, String fallback) {
        if (key == null || key.isBlank()) return fallback;
        return strings.getOrDefault(key, key);
    }

    public void put(String key, String value) {
        if (key == null || key.isBlank()) return;
        strings.put(key, value == null ? "" : value);
    }

    public int size() {
        return strings.size();
    }

    public static LocalizationRepository createStarter() {
        LocalizationRepository repo = new LocalizationRepository();
        repo.put("menu.play", "Play");
        return repo;
    }
}
