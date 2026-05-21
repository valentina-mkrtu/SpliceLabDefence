package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public final class HpBarWidget extends Group {
    private final Table bg;
    private final Table fill;
    private float pct = 1f;

    public HpBarWidget(Skin skin, Color bgColor, Color fillColor) {
        bg = new Table();
        bg.setBackground(skin.newDrawable("white", bgColor));
        bg.setFillParent(true);

        fill = new Table();
        fill.setBackground(skin.newDrawable("white", fillColor));
        fill.setSize(1, 1);

        addActor(bg);
        addActor(fill);
        setSize(120, 10);
    }

    public void setPercent(float pct) {
        this.pct = Math.max(0f, Math.min(1f, pct));
        layoutFill();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutFill();
    }

    private void layoutFill() {
        fill.setPosition(0, 0);
        fill.setSize(getWidth() * pct, getHeight());
    }
}

