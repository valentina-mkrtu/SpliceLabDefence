package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private final Image icon;
    private final Texture slotTexture;
    private Texture iconTexture;
    private String iconTexturePath;

    public GridCellWidget(Skin skin, UiFactory ui, int col, int row) {
        this.col = col;
        this.row = row;
        bg = new Table();
        slotTexture = new Texture(SLOT_ICON_TEXTURE_PATH);
        bg.setBackground(new TextureRegionDrawable(new TextureRegion(slotTexture)));
        bg.getColor().a = 0.9f;
        bg.setFillParent(true);

        icon = new Image();
        icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        bg.add(icon).size(58f, 58f).center();
        addActor(bg);
        setSize(110, 110);
    }

    public void dispose() {
        slotTexture.dispose();
        if (iconTexture != null) iconTexture.dispose();
    }

    public void setIcon(String texturePath) {
        if (texturePath != null && texturePath.isBlank()) texturePath = null;
        if ((iconTexturePath == null && texturePath == null)
                || (iconTexturePath != null && iconTexturePath.equals(texturePath))) {
            return;
        }

        if (iconTexture != null) {
            iconTexture.dispose();
            iconTexture = null;
        }
        iconTexturePath = texturePath;

        if (texturePath == null) {
            icon.setDrawable(null);
            return;
        }

        iconTexture = new Texture(texturePath);
        icon.setDrawable(new TextureRegionDrawable(new TextureRegion(iconTexture)));
    }

    public TextureRegionDrawable getIconDrawable() {
        var drawable = icon.getDrawable();
        if (drawable instanceof TextureRegionDrawable trd) return trd;
        return null;
    }

    public float getIconLocalX(float actorLocalX) {
        bg.validate();
        return actorLocalX - icon.getX();
    }

    public float getIconLocalY(float actorLocalY) {
        bg.validate();
        return actorLocalY - icon.getY();
    }

    public float getIconWidth() {
        return icon.getWidth();
    }

    public float getIconHeight() {
        return icon.getHeight();
    }

    public void setIconVisible(boolean visible) {
        icon.setVisible(visible);
    }

    public String getLabelText() {
        return "";
    }
}
