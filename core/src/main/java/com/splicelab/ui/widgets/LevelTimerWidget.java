package com.splicelab.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.splicelab.ui.UiFactory;

public final class LevelTimerWidget extends Table {
    private final Label label;
    private final Table barFill;

    private float totalSeconds;
    private float secondsRemaining;

    public LevelTimerWidget(Skin skin, UiFactory ui) {
        super(skin);

        setBackground(skin.newDrawable("white", 0f, 0f, 0f, 0.35f));

        barFill = new Table();
        barFill.setBackground(skin.newDrawable("white", 0.85f, 0.22f, 0.22f, 0.9f));
        addActor(barFill);

        label = ui.label("00:00");
        label.setAlignment(Align.center);
        add(label).pad(4).center();
    }

    public void setTotalSeconds(float seconds) {
        totalSeconds = Math.max(0f, seconds);
        layoutBar();
    }

    public void setSeconds(float seconds) {
        secondsRemaining = Math.max(0f, seconds);

        int s = Math.max(0, (int) Math.ceil(secondsRemaining));
        int m = s / 60;
        int r = s % 60;
        label.setText(String.format("%02d:%02d", m, r));

        layoutBar();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutBar();
    }

    @Override
    public void layout() {
        super.layout();
        layoutBar();
    }

    private void layoutBar() {
        float pct;
        if (totalSeconds <= 0f) {
            pct = 1f;
        } else {
            pct = secondsRemaining / totalSeconds;
            pct = Math.max(0f, Math.min(1f, pct));
        }

        barFill.setPosition(0f, 0f);
        barFill.setSize(getWidth() * pct, getHeight());
        barFill.toBack();
    }
}
