package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.splicelab.ui.UiFactory;

public final class GridCellWidget extends Group {
    private static final String SLOT_ICON_TEXTURE_PATH = "art/icons/slot.png";

    public final int col;
    public final int row;
    private final Table bg;
    private final Label text;
    private final Texture slotTexture;

    public GridCellWidget(Skin skin, UiFactory ui, int col, int row) {
        this.col = col;
        this.row = row;
        bg = new Table();
        slotTexture = new Texture(SLOT_ICON_TEXTURE_PATH);
        bg.setBackground(new TextureRegionDrawable(new TextureRegion(slotTexture)));
        bg.getColor().a = 0.9f;
        bg.setFillParent(true);

        text = ui.label("");
        bg.add(text).pad(2);
        addActor(bg);
        setSize(110, 110);
    }

    public void dispose() {
        slotTexture.dispose();
    }

    public void setLabel(String label) {
        text.setText(label == null ? "" : label);
    }

    public String getLabelText() {
        return text.getText().toString();
    }
}
