package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.ui.UiFactory;

public final class ConveyorSlotWidget extends Group {
    public final boolean leftSide;
    public final int index;
    private final Table bg;
    private final Label label;
    private final Skin skin;
    private final com.splicelab.ui.widgets.HpBarWidget hpBar;

    public ConveyorSlotWidget(Skin skin, UiFactory ui, boolean leftSide, int index) {
        this.skin = skin;
        this.leftSide = leftSide;
        this.index = index;

        bg = new Table();
        bg.setFillParent(true);
        label = ui.label("");
        bg.add(label);
        addActor(bg);

        hpBar = new com.splicelab.ui.widgets.HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.25f, 0.9f, 0.35f, 1f));
        hpBar.setPosition(6, 6);
        hpBar.setSize(108, 8);
        hpBar.setVisible(false);
        addActor(hpBar);
        setSize(120, 70);
        setLocked(true);
    }

    public void setLocked(boolean locked) {
        Color c = locked ? new Color(0.25f, 0.1f, 0.1f, 1f) : new Color(0.1f, 0.25f, 0.12f, 1f);
        bg.setBackground(skin.newDrawable("white", c));
    }

    public void setText(String txt) {
        label.setText(txt == null ? "" : txt);
    }

    public void setHpPercent(float pct) {
        hpBar.setPercent(pct);
        hpBar.setVisible(pct < 0.999f);
    }
}
