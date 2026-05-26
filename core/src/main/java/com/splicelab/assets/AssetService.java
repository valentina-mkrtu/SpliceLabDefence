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
        // Main lobby backgrounds.
        assetManager.load("art/backgrounds/mainbg.png", Texture.class);
        assetManager.load("art/backgrounds/menubg.png", Texture.class);
        assetManager.load("art/backgrounds/thelab.png", Texture.class);
        assetManager.load("art/backgrounds/themap.png", Texture.class);
        assetManager.load("art/backgrounds/currbg.png", Texture.class);

        // Dialog/window backgrounds.
        assetManager.load("art/backgrounds/menuwindowbg.png", Texture.class);
        assetManager.load("art/backgrounds/levelsbg.png", Texture.class);
        assetManager.load("art/backgrounds/levels.png", Texture.class);
        assetManager.load("art/backgrounds/settingsbg.png", Texture.class);

        // Main lobby icons.
        assetManager.load("art/icons/iconbg.png", Texture.class);
        assetManager.load("art/icons/account.png", Texture.class);
        assetManager.load("art/icons/collections.png", Texture.class);
        assetManager.load("art/icons/entities.png", Texture.class);
        assetManager.load("art/icons/shop.png", Texture.class);
        assetManager.load("art/icons/pfp.png", Texture.class);
        assetManager.load("art/icons/lab.png", Texture.class);
        assetManager.load("art/icons/settings.png", Texture.class);
        assetManager.load("art/icons/dna.png", Texture.class);
        assetManager.load("art/icons/cry.png", Texture.class);
        assetManager.load("art/icons/sound.png", Texture.class);
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
