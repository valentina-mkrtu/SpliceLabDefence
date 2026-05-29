package com.splicelab.ui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.splicelab.ui.UiFactory;

public final class CurrencyPillWidget extends Table {
    private final Label amountLabel;
    private final Image iconImage;

    // Header layout spec: box 170x64, icon 28, amount font ~24px.
    private static final float BOX_W = 170f;
    private static final float BOX_H = 64f;
    private static final float ICON_SIZE = 28f;
    private static final float ICON_SCALE = 1f;
    private static final float AMOUNT_SCALE = 1.85f;

    private static final float ICON_SIZE_SMALL_SCREEN = 22f;
    private static final float ICON_SIZE_MEDIUM_SCREEN = 24f;
    private static final float AMOUNT_SCALE_SMALL_SCREEN = 1.55f;

    public CurrencyPillWidget(Skin skin, UiFactory ui, Drawable background, Drawable icon, String label, int amount) {
        setTouchable(Touchable.disabled);
        if (background instanceof TextureRegionDrawable tr) {
            // The source art is very tall; override min sizes so the pill height is driven by
            // its contents instead of the raw texture dimensions.
            TextureRegionDrawable bg = new TextureRegionDrawable(tr);
            bg.setMinHeight(BOX_H);
            bg.setMinWidth(BOX_W);
            setBackground(bg);
        } else if (background != null) {
            setBackground(background);
        }
        defaults().pad(0, 16, 0, 16);
        setClip(true);

        // Keep the pill layout size stable, but scale visuals up.
        iconImage = icon != null ? new Image(icon) : null;
        // Keep visuals inside the box even on small screens.
        float iconSize = ICON_SIZE_MEDIUM_SCREEN;
        float amountScale = AMOUNT_SCALE;
        if (Gdx.graphics != null) {
            int screenW = Gdx.graphics.getWidth();
            if (screenW >= 1000) {
                iconSize = ICON_SIZE;
            } else if (screenW < 700) {
                iconSize = ICON_SIZE_SMALL_SCREEN;
                amountScale = AMOUNT_SCALE_SMALL_SCREEN;
            }
        }

        if (iconImage != null) {
            iconImage.setOrigin(Align.center);
            iconImage.setScale(ICON_SCALE);
            iconImage.setScaling(Scaling.fit);
            add(iconImage).size(iconSize).padRight(12).center();
        }
        if (label != null && !label.isBlank()) {
            var labelActor = ui.smallLabel(label);
            labelActor.setFontScale(1.05f);
            add(labelActor).padRight(6);
        }
        amountLabel = ui.label(formatAmount(amount));
        amountLabel.setFontScale(amountScale);
        amountLabel.setAlignment(Align.right);
        amountLabel.setEllipsis(true);
        add(amountLabel).minWidth(0).expandX().right().center();

        // Force stable preferred size so the header layout stays consistent.
        setSize(BOX_W, BOX_H);
    }

    public void setAmount(int amount) {
        amountLabel.setText(formatAmount(amount));
    }

    @Override
    public float getPrefWidth() {
        return BOX_W;
    }

    @Override
    public float getPrefHeight() {
        return BOX_H;
    }

    private static String formatAmount(int amount) {
        int abs = Math.abs(amount);
        if (abs < 10_000) return String.valueOf(amount);
        if (abs < 1_000_000) return formatCompact(amount, 1_000, 'K');
        if (abs < 1_000_000_000) return formatCompact(amount, 1_000_000, 'M');
        return formatCompact(amount, 1_000_000_000, 'B');
    }

    private static String formatCompact(int amount, int unit, char suffix) {
        float v = amount / (float) unit;
        float abs = Math.abs(v);
        // Keep string short so it always fits inside the pill.
        String s;
        if (abs >= 100) s = String.format(java.util.Locale.ROOT, "%.0f", v);
        else if (abs >= 10) s = String.format(java.util.Locale.ROOT, "%.1f", v);
        else s = String.format(java.util.Locale.ROOT, "%.2f", v);
        // Strip trailing .0 / .00
        if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
        else if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s + suffix;
    }

    public static Drawable drawableFromTexture(Texture texture) {
        if (texture == null) return null;
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
