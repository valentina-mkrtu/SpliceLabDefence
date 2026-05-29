package com.splicelab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public final class UiStyle {
    private UiStyle() {
    }

    public static final Color PANEL_DARK = new Color(0.08f, 0.09f, 0.12f, 0.96f);
    public static final Color CARD_DARK = new Color(0.12f, 0.13f, 0.17f, 1f);
    public static final Color DIM_OVERLAY = new Color(0f, 0f, 0f, 0.6f);

    public static final Color DNA_ACCENT = new Color(0.25f, 0.95f, 0.35f, 1f);
    public static final Color CRY_ACCENT = new Color(0.25f, 0.85f, 0.95f, 1f);
    public static final Color DEFEAT_ACCENT = new Color(0.95f, 0.25f, 0.25f, 1f);

    public static Table panel(Skin skin) {
        Table t = new Table();
        t.setBackground(skin.newDrawable("white", PANEL_DARK));
        return t;
    }

    public static Table cardPanel(Skin skin) {
        Table t = new Table();
        t.setBackground(skin.newDrawable("white", CARD_DARK));
        return t;
    }

    public static Drawable dimBackground(Skin skin) {
        return skin.newDrawable("white", DIM_OVERLAY);
    }

    public static TextButton primaryButton(Skin skin, String text) {
        TextButton b = new TextButton(text, skin);
        b.getLabel().setFontScale(1.05f);
        b.setColor(DNA_ACCENT);
        return b;
    }

    public static Label header(Skin skin, String text) {
        Label l = new Label(text, skin);
        l.setFontScale(1.6f);
        l.setAlignment(Align.center);
        return l;
    }
}

