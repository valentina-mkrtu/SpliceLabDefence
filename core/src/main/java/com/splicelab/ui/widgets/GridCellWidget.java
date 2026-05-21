package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.ui.UiFactory;

public final class GridCellWidget extends Group {
    public final int col;
    public final int row;
    private final Table bg;
    private final Label text;

    public GridCellWidget(Skin skin, UiFactory ui, int col, int row) {
        this.col = col;
        this.row = row;
        bg = new Table();
        bg.setBackground(skin.newDrawable("white", new Color(0.18f, 0.18f, 0.22f, 1f)));
        bg.setFillParent(true);

        text = ui.label("");
        bg.add(text).pad(2);
        addActor(bg);
        setSize(110, 110);
    }

    public void setLabel(String label) {
        text.setText(label == null ? "" : label);
    }
}

