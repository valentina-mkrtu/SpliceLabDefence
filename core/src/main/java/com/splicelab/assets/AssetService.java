package com.splicelab.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public final class AssetService {
    private final AssetManager assetManager = new AssetManager();

    public void loadMinimal() {
        // Keep LoadingScreen visible long enough to feel real.
        // Add any other small/critical UI assets here.
        assetManager.load("art/backgrounds/loading.png", Texture.class);
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

    public void dispose() {
        assetManager.dispose();
    }
}
