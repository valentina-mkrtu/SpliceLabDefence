package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.splicelab.assets.AssetService;

/**
 * Builds the close-button image for dialogs.
 *
 * <p>Prefers the icon from the shared {@link AssetService}. If not loaded (e.g. during
 * first-run before asset manager finishes), falls back to a programmatically drawn ✕
 * using a Pixmap — this fallback-texture IS privately owned and disposed by {@link CloseImage#dispose()}.
 * (T-2.2, T-3.4)</p>
 */
final class DialogCloseImageFactory {
    static final String ICON_CLOSE_PATH = "art/icons/converted/close_128.png";

    private DialogCloseImageFactory() {}

    /**
     * @param assets Shared asset service. If the close icon is already loaded it will be used
     *               and no texture is privately allocated.  Pass {@code null} to always use
     *               the pixmap fallback (e.g. in tests).
     */
    static CloseImage create(AssetService assets) {
        // Prefer shared drawable — no private allocation needed.
        if (assets != null) {
            Drawable sharedDrawable = assets.getDrawable(ICON_CLOSE_PATH);
            if (sharedDrawable != null) {
                Image img = new Image(sharedDrawable);
                img.setTouchable(Touchable.enabled);
                return new CloseImage(img, null); // null = no private texture to dispose
            }
        }

        // Fallback: draw an ✕ via Pixmap (privately owned, disposed by CloseImage).
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(new Color(0.07f, 0.16f, 0.45f, 1f));
        int thickness = 5;
        for (int t = -thickness; t <= thickness; t++) {
            pixmap.drawLine(10, 10 + t, 53, 53 + t);
            pixmap.drawLine(10, 53 + t, 53, 10 + t);
        }
        Texture closeTex = new Texture(pixmap);
        pixmap.dispose();

        Image img = new Image(new TextureRegionDrawable(new TextureRegion(closeTex)));
        img.setTouchable(Touchable.enabled);
        return new CloseImage(img, closeTex);
    }

    static final class CloseImage implements Disposable {
        final Image image;
        /** Non-null only when the close icon was created from the pixmap fallback. */
        private final Texture privateTexture;

        private CloseImage(Image image, Texture privateTexture) {
            this.image = image;
            this.privateTexture = privateTexture;
        }

        @Override
        public void dispose() {
            if (privateTexture != null) privateTexture.dispose();
        }
    }
}

