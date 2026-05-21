package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.ui.UiFactory;

public final class TubeWidget extends Group {
    private final Table bg;
    private final Label label;

    public TubeWidget(Skin skin, UiFactory ui) {
        bg = new Table();
        bg.setBackground(skin.newDrawable("white", new Color(0.2f, 0.25f, 0.3f, 1f)));
        bg.setFillParent(true);

        label = ui.label("TUBE");
        bg.add(label);
        addActor(bg);
        setSize(110, 110);
    }
}

