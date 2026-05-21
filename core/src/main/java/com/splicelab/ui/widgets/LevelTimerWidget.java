package com.splicelab.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.ui.UiFactory;

public final class LevelTimerWidget extends Table {
    private final Label label;

    public LevelTimerWidget(Skin skin, UiFactory ui) {
        super(skin);
        setBackground(skin.newDrawable("white", 0f, 0f, 0f, 0.35f));
        label = ui.label("00:00");
        add(label).pad(4);
    }

    public void setSeconds(float seconds) {
        int s = Math.max(0, (int) Math.ceil(seconds));
        int m = s / 60;
        int r = s % 60;
        label.setText(String.format("%02d:%02d", m, r));
    }
}

