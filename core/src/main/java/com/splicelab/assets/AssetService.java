package com.splicelab.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public final class AssetService {
    private final AssetManager assetManager = new AssetManager();

    public void loadMinimal() {
        // Keep LoadingScreen visible long enough to feel real.
        // Add any other small/critical UI assets here.
        assetManager.load("art/backgrounds/loading.png", Texture.class);
        assetManager.load("art/backgrounds/menuwindowbg.png", Texture.class);
    }

    public void loadUi() {
        assetManager.load("art/backgrounds/menuwindowbg.png", Texture.class);
    }

    public boolean update() {
        return assetManager.update();
    }

    public float getProgress() {
        return assetManager.getProgress();
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public Texture getTexture(String path) {
        if (path == null || path.isBlank()) return null;
        if (!assetManager.isLoaded(path, Texture.class)) return null;
        return assetManager.get(path, Texture.class);
    }

    public void dispose() {
        assetManager.dispose();
    }
}
