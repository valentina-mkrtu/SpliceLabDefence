package com.splicelab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public final class UiFactory {
    private final Skin skin;

    public UiFactory(Skin skin) {
        this.skin = skin;
    }

    public TextButton textButton(String text) {
        return new TextButton(text, skin);
    }

    public Label label(String text) {
        return new Label(text, new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE));
    }

    public Table panel() {
        Table t = new Table();
        t.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));
        return t;
    }
}

