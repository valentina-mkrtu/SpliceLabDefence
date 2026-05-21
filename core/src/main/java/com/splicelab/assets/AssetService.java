package com.splicelab.assets;

import com.badlogic.gdx.assets.AssetManager;

public final class AssetService {
    private final AssetManager assetManager = new AssetManager();

    public void loadMinimal() {
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

