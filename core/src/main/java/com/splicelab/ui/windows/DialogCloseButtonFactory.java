package com.splicelab.ui.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

final class DialogCloseButtonFactory {
    private static final String ICON_BG_PATH = "art/icons/iconbg.png";
    private static final String ICON_CLOSE_PATH = "art/icons/close.png";

    private DialogCloseButtonFactory() {
    }

    static CloseButton create(Skin skin) {
        Texture bgTex = Gdx.files.internal(ICON_BG_PATH).exists() ? new Texture(Gdx.files.internal(ICON_BG_PATH)) : null;
        Texture closeTex = Gdx.files.internal(ICON_CLOSE_PATH).exists() ? new Texture(Gdx.files.internal(ICON_CLOSE_PATH)) : null;

        if (bgTex != null) bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (closeTex != null) closeTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = bgTex == null ? skin.newDrawable("white") : new TextureRegionDrawable(new TextureRegion(bgTex));
        style.down = style.up;
        if (closeTex != null) {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(closeTex));
            style.imageDown = style.imageUp;
        }

        ImageButton btn = new ImageButton(style);
        btn.setTouchable(Touchable.enabled);
        return new CloseButton(btn, bgTex, closeTex);
    }

    static final class CloseButton implements Disposable {
        final ImageButton button;
        private final Texture bgTex;
        private final Texture closeTex;

        private CloseButton(ImageButton button, Texture bgTex, Texture closeTex) {
            this.button = button;
            this.bgTex = bgTex;
            this.closeTex = closeTex;
        }

        @Override
        public void dispose() {
            if (bgTex != null) bgTex.dispose();
            if (closeTex != null) closeTex.dispose();
        }
    }
}

