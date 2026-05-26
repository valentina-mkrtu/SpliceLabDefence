package com.splicelab.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public final class TextureCache {
    private final Map<String, Texture> cache = new HashMap<>();

    public Texture get(String internalPath) {
        if (internalPath == null || internalPath.isBlank()) return null;
        Texture existing = cache.get(internalPath);
        if (existing != null) return existing;

        if (!Gdx.files.internal(internalPath).exists()) {
            return null;
        }

        Texture created = new Texture(Gdx.files.internal(internalPath));
        cache.put(internalPath, created);
        return created;
    }

    public Texture getOrMakeSolid(String key, Color color) {
        if (key == null || key.isBlank()) return null;
        Texture existing = cache.get(key);
        if (existing != null) return existing;

        Pixmap p = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        p.setColor(color);
        p.fill();
        Texture created = new Texture(p);
        p.dispose();
        cache.put(key, created);
        return created;
    }

    public void dispose() {
        for (Texture t : cache.values()) {
            if (t != null) t.dispose();
        }
        cache.clear();
    }
}

