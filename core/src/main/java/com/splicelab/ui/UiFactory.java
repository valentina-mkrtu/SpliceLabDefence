package com.splicelab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.splicelab.audio.AudioService;

public final class UiFactory {
    private final Skin skin;
    private final AudioService audio;

    public UiFactory(Skin skin) {
        this(skin, null);
    }

    public UiFactory(Skin skin, AudioService audio) {
        this.skin = skin;
        this.audio = audio;
    }

    public TextButton textButton(String text) {
        TextButton b = new TextButton(text, skin);
        b.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (audio != null) audio.playButtonClick();
            }
        });
        return b;
    }

    public Label label(String text) {
        return new Label(text, new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE));
    }

    public Label smallLabel(String text) {
        return new Label(text, new Label.LabelStyle(skin.getFont("default-font"), new Color(1f, 1f, 1f, 0.75f)));
    }

    public Table panel() {
        Table t = new Table();
        t.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));
        return t;
    }
}
