package com.splicelab.ui.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.splicelab.ui.UiFactory;

public final class CurrencyPillWidget extends Table {
    private final Label amountLabel;

    public CurrencyPillWidget(Skin skin, UiFactory ui, Drawable background, Drawable icon, int amount) {
        setTouchable(Touchable.disabled);
        if (background != null) setBackground(background);
        defaults().pad(4);

        if (icon != null) add(new Image(icon)).size(18).padRight(6);
        amountLabel = ui.label(String.valueOf(amount));
        add(amountLabel);
    }

    public void setAmount(int amount) {
        amountLabel.setText(String.valueOf(amount));
    }

    public static Drawable drawableFromTexture(Texture texture) {
        if (texture == null) return null;
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
