package com.splicelab.ui.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

final class DialogCloseImageFactory {
    private static final String ICON_CLOSE_PATH = "art/icons/converted/close_128.png";

    private DialogCloseImageFactory() {
    }

    static CloseImage create() {
        Texture closeTex = null;
        if (Gdx.files.internal(ICON_CLOSE_PATH).exists()) {
            try {
                closeTex = new Texture(Gdx.files.internal(ICON_CLOSE_PATH));
                closeTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            } catch (Throwable t) {
                closeTex = null;
            }
        }

        if (closeTex == null) {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();
            pixmap.setColor(new Color(0.07f, 0.16f, 0.45f, 1f));
            int thickness = 5;
            for (int t = -thickness; t <= thickness; t++) {
                pixmap.drawLine(10, 10 + t, 53, 53 + t);
                pixmap.drawLine(10, 53 + t, 53, 10 + t);
            }
            closeTex = new Texture(pixmap);
            pixmap.dispose();
        }

        Image img = new Image(new TextureRegionDrawable(new TextureRegion(closeTex)));
        img.setTouchable(Touchable.enabled);
        return new CloseImage(img, closeTex);
    }

    static final class CloseImage implements Disposable {
        final Image image;
        private final Texture texture;

        private CloseImage(Image image, Texture texture) {
            this.image = image;
            this.texture = texture;
        }

        @Override
        public void dispose() {
            if (texture != null) texture.dispose();
        }
    }
}

