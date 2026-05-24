package com.splicelab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class Scene2dPlaceholders {
    private Scene2dPlaceholders() {
    }

    public static Image coloredSquare(Skin skin, Color color) {
        Texture texture = new Texture(makePixmap(8, 8, color));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return new Image(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    public static Label iconLabel(Skin skin, String text) {
        BitmapFont font = skin.getFont("default-font");
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        return new Label(text, style);
    }

    private static Pixmap makePixmap(int w, int h, Color color) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return pixmap;
    }
}

