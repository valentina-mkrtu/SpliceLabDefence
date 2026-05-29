package com.splicelab.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for all file-backed textures.
 *
 * <h3>Asset optimisation TODO (T-2.3)</h3>
 * <ul>
 *   <li>Backgrounds: downscale mainbg.png (13 MB), lab_game_bg.png (12 MB), loading.png (8.6 MB)
 *       to max 1080×1920; re-compress with pngquant/oxipng or convert to high-quality JPG.</li>
 *   <li>Icons: re-export settings.png (~2.6 MB) at its actual display size (128–256 px).</li>
 *   <li>Atlas: pack fusion sprites + UI icons into a TexturePacker atlas so the GPU loads one
 *       page instead of dozens of individual textures.</li>
 *   <li>Audio: a.wav (884 KB uncompressed duplicate) has been removed.
 *       labmusic.mp3 and mainpage.mp3 (~3.8 MB each) are acceptable as MP3/OGG.</li>
 * </ul>
 *
 * <p>All textures used by the game are registered here and loaded through the libGDX
 * {@link AssetManager}.  Callers must never call {@code new Texture(path)} for file-backed
 * assets; use {@link #getTexture(String)} or {@link #getDrawable(String)} instead.
 * This guarantees deduplication, avoids blocking disk I/O on the render thread, and
 * centralises disposal.</p>
 *
 * <p>The drawable cache in {@link #getDrawable(String)} avoids re-wrapping the same
 * {@link Texture} in a new {@link TextureRegionDrawable} on every call site. (T-2.2)</p>
 */
public final class AssetService {
    private final AssetManager assetManager = new AssetManager();
    /** Cache of path → drawable wrapper so callers don't re-allocate wrappers per frame. */
    private final Map<String, TextureRegionDrawable> drawableCache = new HashMap<>();

    private void loadTextureIfExists(String path) {
        if (path == null || path.isBlank()) return;
        if (!Gdx.files.internal(path).exists()) return;
        assetManager.load(path, Texture.class);
    }

    // -------------------------------------------------------------------------
    // Loading phases
    // -------------------------------------------------------------------------

    public void loadMinimal() {
        // Keep LoadingScreen visible long enough to feel real.
        // Add any other small/critical UI assets here.
        assetManager.load("art/backgrounds/loading.png", Texture.class);
        assetManager.load("art/backgrounds/menuwindowbg.png", Texture.class);
    }

    public void loadUi() {
        // --- Backgrounds ---
        assetManager.load("art/backgrounds/mainbg.png", Texture.class);
        assetManager.load("art/backgrounds/menubg.png", Texture.class);
        assetManager.load("art/backgrounds/thelab.png", Texture.class);
        assetManager.load("art/backgrounds/themap.png", Texture.class);
        assetManager.load("art/backgrounds/currbg.png", Texture.class);
        assetManager.load("art/backgrounds/menuwindowbg.png", Texture.class);
        assetManager.load("art/backgrounds/levelsbg.png", Texture.class);
        assetManager.load("art/backgrounds/levels.png", Texture.class);
        assetManager.load("art/backgrounds/settingsbg.png", Texture.class);
        loadTextureIfExists("art/backgrounds/settingbg.png");
        assetManager.load("art/backgrounds/lab_game_bg.png", Texture.class);
        assetManager.load("art/backgrounds/fusion_station.png", Texture.class);
        assetManager.load("art/backgrounds/shaft.png", Texture.class);
        assetManager.load("art/backgrounds/levelend.png", Texture.class);

        // --- General UI icons ---
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
        assetManager.load("art/icons/converted/close_128.png", Texture.class);
        assetManager.load("art/icons/triangle.png", Texture.class);
        assetManager.load("art/icons/hp.png", Texture.class);
        assetManager.load("art/icons/claim.png", Texture.class);
        loadTextureIfExists("art/icons/doubleclaim.png");

        // --- Grid / belt ---
        assetManager.load("art/icons/slot.png", Texture.class);
        assetManager.load("art/icons/tube.png", Texture.class);

        // --- Boost icons ---
        assetManager.load("art/icons/freeze.png", Texture.class);
        assetManager.load("art/icons/cooldown.png", Texture.class);
        assetManager.load("art/icons/x2.png", Texture.class);
        assetManager.load("art/icons/hp.png", Texture.class);
        assetManager.load("art/icons/remove.png", Texture.class);

        // --- Enemy sprites ---
        assetManager.load("art/enemies/reg1.png", Texture.class);
        assetManager.load("art/enemies/reg2.png", Texture.class);
        assetManager.load("art/enemies/reg3.png", Texture.class);
        assetManager.load("art/enemies/boss1.png", Texture.class);
        assetManager.load("art/enemies/boss2.png", Texture.class);

        // --- Entity sprites ---
        assetManager.load("art/entities/slime.png", Texture.class);
        assetManager.load("art/entities/mech.png", Texture.class);
        assetManager.load("art/entities/fungy.png", Texture.class);

        // --- Item icons ---
        assetManager.load("art/items/battery.png", Texture.class);
        assetManager.load("art/items/criogel.png", Texture.class);
        assetManager.load("art/items/crystalshard.png", Texture.class);
        assetManager.load("art/items/nanobots.png", Texture.class);
        assetManager.load("art/items/radioactivegoo.png", Texture.class);
        assetManager.load("art/items/toxicwaste.png", Texture.class);

        // --- Conveyor belt ---
        assetManager.load("spine/production-line/belt.png", Texture.class);
        assetManager.load("spine/production-line/belt_line.png", Texture.class);

        // --- Fusion sprites ---
        assetManager.load("art/fusions/criofungy.png", Texture.class);
        assetManager.load("art/fusions/criomech.png", Texture.class);
        assetManager.load("art/fusions/crioslime.png", Texture.class);
        assetManager.load("art/fusions/crystalfungy.png", Texture.class);
        assetManager.load("art/fusions/crystalmech.png", Texture.class);
        assetManager.load("art/fusions/crystalslime.png", Texture.class);
        assetManager.load("art/fusions/electrofungy.png", Texture.class);
        assetManager.load("art/fusions/electroslime.png", Texture.class);
        assetManager.load("art/fusions/mechbot.png", Texture.class);
        assetManager.load("art/fusions/nanofungy.png", Texture.class);
        assetManager.load("art/fusions/nanomechbot.png", Texture.class);
        assetManager.load("art/fusions/nanoslime.png", Texture.class);
        assetManager.load("art/fusions/radioactivefungy.png", Texture.class);
        assetManager.load("art/fusions/radioactivemech.png", Texture.class);
        assetManager.load("art/fusions/radioactiveslime.png", Texture.class);
        assetManager.load("art/fusions/toxicfungy.png", Texture.class);
        assetManager.load("art/fusions/toxicmech.png", Texture.class);
        assetManager.load("art/fusions/toxicslime.png", Texture.class);
    }

    // -------------------------------------------------------------------------
    // Asset access
    // -------------------------------------------------------------------------

    public boolean update() {
        return assetManager.update();
    }

    public float getProgress() {
        return assetManager.getProgress();
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    /**
     * Returns the loaded {@link Texture} for {@code path}, or {@code null} if not yet loaded.
     * Callers should treat a {@code null} return as a graceful missing-asset fallback.
     */
    public Texture getTexture(String path) {
        if (path == null || path.isBlank()) return null;
        if (!assetManager.isLoaded(path, Texture.class)) return null;
        return assetManager.get(path, Texture.class);
    }

    /**
     * Returns a cached {@link TextureRegionDrawable} for {@code path}.
     * The wrapper is created once and reused on every subsequent call — callers must not
     * dispose it (it is owned by this cache).  Returns {@code null} if the texture is not loaded.
     */
    public TextureRegionDrawable getDrawable(String path) {
        Texture t = getTexture(path);
        if (t == null) return null;
        return drawableCache.computeIfAbsent(path, p -> new TextureRegionDrawable(new TextureRegion(t)));
    }

    public void dispose() {
        drawableCache.clear();
        assetManager.dispose();
    }
}
