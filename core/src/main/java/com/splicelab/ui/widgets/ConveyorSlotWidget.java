package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.assets.AssetService;
import com.splicelab.ui.UiFactory;

/** A conveyor belt slot widget. Textures are owned by the shared {@link AssetService}. (T-2.2) */
public final class ConveyorSlotWidget extends Group {

    public final boolean leftSide;
    public final int index;
    private final Table bg;
    private final Label label;
    private final Skin skin;
    private final HpBarWidget hpBar;

    public ConveyorSlotWidget(Skin skin, UiFactory ui, AssetService assets, boolean leftSide, int index) {
        this.skin = skin;
        this.leftSide = leftSide;
        this.index = index;

        bg = new Table();
        bg.setFillParent(true);

        var slotDrawable = assets.getDrawable(GridCellWidget.SLOT_ICON_TEXTURE_PATH);
        Image slotImage = slotDrawable != null ? new Image(slotDrawable) : new Image();
        bg.add(slotImage).grow();
        bg.row();
        label = ui.label("");
        bg.add(label);
        addActor(bg);

        hpBar = new HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.25f, 0.9f, 0.35f, 1f));
        hpBar.setPosition(6, 6);
        hpBar.setSize(108, 8);
        hpBar.setVisible(false);
        addActor(hpBar);
        setSize(120, 70);
        setLocked(true);
    }

    /** No-op: texture owned by {@link AssetService}. */
    public void dispose() {}

    public void setLocked(boolean locked) {
        Color c = locked ? new Color(1f, 1f, 1f, 0.35f) : new Color(1f, 1f, 1f, 1f);
        bg.setColor(c);
    }

    public void setText(String txt) {
        label.setText(txt == null ? "" : txt);
    }

    public void setHpPercent(float pct) {
        hpBar.setPercent(pct);
        hpBar.setVisible(pct < 0.999f);
    }
}
