package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.splicelab.ui.UiFactory;

public final class TubeWidget extends Group {
    private static final String TUBE_ICON_TEXTURE_PATH = "art/icons/tube.png";

    private final Table bg;
    private final Texture iconTexture;
    private final Image icon;
    private final Table cooldownBar;
    private float cooldownPct;

    public TubeWidget(Skin skin, UiFactory ui) {
        bg = new Table();
        // Let the gameplay background show through.
        bg.setBackground(skin.newDrawable("white", new Color(0.2f, 0.25f, 0.3f, 0.0f)));
        bg.setFillParent(true);

        iconTexture = new Texture(TUBE_ICON_TEXTURE_PATH);
        icon = new Image(new TextureRegion(iconTexture));
        bg.add(icon).grow();
        addActor(bg);

        cooldownBar = new Table();
        cooldownBar.setBackground(skin.newDrawable("white", new Color(1f, 1f, 0.2f, 0.85f)));
        addActor(cooldownBar);
        setSize(110, 110);
    }

    public void dispose() {
        iconTexture.dispose();
    }

    public void setCooldown(float remainingSeconds, float totalSeconds) {
        float total = Math.max(0.001f, totalSeconds);
        float rem = Math.max(0f, remainingSeconds);
        cooldownPct = Math.min(1f, rem / total);
        updateCooldownLayout();
    }

    private void updateCooldownLayout() {
        float pad = 6f;
        float w = getWidth() - pad * 2f;
        float h = 10f;
        cooldownBar.setVisible(cooldownPct > 0f);
        cooldownBar.setSize(w * cooldownPct, h);
        cooldownBar.setPosition(pad, pad);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        updateCooldownLayout();
    }
}
