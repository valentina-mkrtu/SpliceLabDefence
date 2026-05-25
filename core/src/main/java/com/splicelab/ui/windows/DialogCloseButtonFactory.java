package com.splicelab.ui.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

final class DialogCloseButtonFactory {
    private static final String ICON_CLOSE_PATH = "art/icons/converted/close_128.png";

    private DialogCloseButtonFactory() {
    }

    static CloseButton create(Skin skin) {
        Texture closeTex = Gdx.files.internal(ICON_CLOSE_PATH).exists() ? new Texture(Gdx.files.internal(ICON_CLOSE_PATH)) : null;

        if (closeTex != null) closeTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        // Force fully-transparent backgrounds.
        Drawable transparent = skin.newDrawable("white", new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0f));
        style.up = transparent;
        style.down = transparent;
        style.over = transparent;
        style.disabled = transparent;
        if (closeTex != null) {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(closeTex));
            style.imageDown = style.imageUp;
            style.imageOver = style.imageUp;
        }

        ImageButton btn = new ImageButton(style);
        btn.setTouchable(Touchable.enabled);
        return new CloseButton(btn, closeTex);
    }

    static final class CloseButton implements Disposable {
        final ImageButton button;
        private final Texture closeTex;

        private CloseButton(ImageButton button, Texture closeTex) {
            this.button = button;
            this.closeTex = closeTex;
        }

        @Override
        public void dispose() {
            if (closeTex != null) closeTex.dispose();
        }
    }
}
