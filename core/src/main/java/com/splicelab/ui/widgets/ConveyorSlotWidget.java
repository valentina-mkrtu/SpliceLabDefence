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

    public ConveyorSlotWidget(Skin skin, UiFactory ui, boolean leftSide, int index) {
        this.skin = skin;
        this.leftSide = leftSide;
        this.index = index;

        bg = new Table();
        bg.setFillParent(true);
        label = ui.label("");
        bg.add(label);
        addActor(bg);
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
}

