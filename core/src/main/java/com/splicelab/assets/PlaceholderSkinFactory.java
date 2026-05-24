package com.splicelab.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class PlaceholderSkinFactory {
    private PlaceholderSkinFactory() {
    }

    public static Skin create() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        Texture white = makeTexture(Color.WHITE);
        skin.add("white", white);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new TextureRegionDrawable(new TextureRegion(white));
        textButtonStyle.down = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.85f, 0.85f, 0.85f, 1f))));
        skin.add("default", textButtonStyle);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = font;
        windowStyle.background = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.12f, 0.12f, 0.16f, 0.98f))));
        skin.add("default", windowStyle);

        ProgressBar.ProgressBarStyle progressStyle = new ProgressBar.ProgressBarStyle();
        progressStyle.background = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.2f, 0.2f, 0.25f, 1f))));
        progressStyle.knob = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.25f, 0.8f, 0.5f, 1f))));
        progressStyle.knobBefore = progressStyle.knob;
        skin.add("default-horizontal", progressStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.08f, 0.08f, 0.1f, 0.8f))));
        skin.add("default", scrollStyle);

        Gdx.app.log("SpliceLab", "Created placeholder Skin");
        return skin;
    }

    public static void addTextureIfPresent(Skin skin, String skinKey, String assetPath) {
        if (skin == null || skinKey == null || assetPath == null) return;
        if (skin.has(skinKey, Texture.class)) return;

        if (!Gdx.files.internal(assetPath).exists()) {
            Gdx.app.log("SpliceLab", "Missing asset: " + assetPath);
            return;
        }

        Texture texture = new Texture(Gdx.files.internal(assetPath));
        skin.add(skinKey, texture);
    }

    public static TextureRegionDrawable getDrawableIfPresent(Skin skin, String skinKey) {
        if (skin == null || skinKey == null) return null;
        if (!skin.has(skinKey, Texture.class)) return null;
        return new TextureRegionDrawable(new TextureRegion(skin.get(skinKey, Texture.class)));
    }

    public static TextureRegionDrawable getStretchedDrawableIfPresent(Skin skin, String skinKey) {
        // TextureRegionDrawable backgrounds stretch to the widget bounds.
        return getDrawableIfPresent(skin, skinKey);
    }

    private static Texture makeTexture(Color color) {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
