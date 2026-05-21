package com.splicelab.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

        ProgressBar.ProgressBarStyle progressStyle = new ProgressBar.ProgressBarStyle();
        progressStyle.background = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.2f, 0.2f, 0.25f, 1f))));
        progressStyle.knob = new TextureRegionDrawable(new TextureRegion(makeTexture(new Color(0.25f, 0.8f, 0.5f, 1f))));
        progressStyle.knobBefore = progressStyle.knob;
        skin.add("default-horizontal", progressStyle);

        Gdx.app.log("SpliceLab", "Created placeholder Skin");
        return skin;
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
