package com.splicelab.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.splicelab.assets.AssetService;
import com.splicelab.ui.UiFactory;

/**
 * A single cell in the ingredient grid.
 *
 * <p>Textures are sourced from the shared {@link AssetService} — this widget owns no
 * {@link com.badlogic.gdx.graphics.Texture} objects and therefore has nothing to dispose. (T-2.2, T-3.3)</p>
 */
public final class GridCellWidget extends Group {
    static final String SLOT_ICON_TEXTURE_PATH = "art/icons/slot.png";

    public final int col;
    public final int row;
    private final Table bg;
    private final Image icon;
    private final AssetService assets;
    private String iconTexturePath;

    public GridCellWidget(Skin skin, UiFactory ui, AssetService assets, int col, int row) {
        this.assets = assets;
        this.col = col;
        this.row = row;
        bg = new Table();
        TextureRegionDrawable slotDrawable = assets.getDrawable(SLOT_ICON_TEXTURE_PATH);
        if (slotDrawable != null) bg.setBackground(slotDrawable);
        bg.getColor().a = 0.9f;
        bg.setFillParent(true);

        icon = new Image();
        icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        bg.add(icon).size(58f, 58f).center();
        addActor(bg);
        setSize(110, 110);
    }

    /**
     * No-op: this widget does not own any textures; the {@link AssetService} owns them all.
     * Kept for API compatibility — callers that invoke dispose() will not break.
     */
    public void dispose() {
        // Textures are owned by AssetService; nothing to dispose here. (T-3.3)
    }

    public void setIcon(String texturePath) {
        if (texturePath != null && texturePath.isBlank()) texturePath = null;
        if ((iconTexturePath == null && texturePath == null)
                || (iconTexturePath != null && iconTexturePath.equals(texturePath))) {
            return;
        }
        iconTexturePath = texturePath;

        if (texturePath == null) {
            icon.setDrawable(null);
            return;
        }

        TextureRegionDrawable drawable = assets.getDrawable(texturePath);
        icon.setDrawable(drawable); // null is fine — shows nothing gracefully
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
