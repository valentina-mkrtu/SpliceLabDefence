package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class CollectionsDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";
    private static final String FRAME_PATH = "art/icons/iconbg.png";
    private static final String LOCK_PATH = "art/icons/slot.png";
    private static final String FUSION_ELECTROSLIME_PATH = "art/fusions/electroslime.png";

    private Texture bgTex;
    private Texture frameTex;
    private Texture lockTex;
    private Texture electroSlimeTex;

    public CollectionsDialog(Skin skin, GameContext context) {
        super("Collections", skin);

        if (com.badlogic.gdx.Gdx.files.internal(BG_PATH).exists()) {
            bgTex = new Texture(com.badlogic.gdx.Gdx.files.internal(BG_PATH));
            bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            var bg = new TextureRegionDrawable(new TextureRegion(bgTex));
            getContentTable().setBackground(bg);
            getButtonTable().setBackground(bg);
        }

        UiFactory ui = new UiFactory(skin, context.audio);

        Table grid = new Table();
        grid.defaults().pad(8);

        int columns = 3;
        for (int i = 0; i < 18; i++) {
            String fusionId = "FUSION_" + (i + 1);
            boolean unlocked = context.fusionUnlocks.isUnlocked(fusionId);
            grid.add(makeFusionCell(skin, ui, unlocked, fusionId)).width(140).height(170);
            if ((i + 1) % columns == 0) grid.row();
        }

        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        getContentTable().add(scroll).width(440).height(520).pad(10);
        var close = ui.textButton("Close");
        close.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        getButtonTable().add(close);

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(12);

        setSize(480, 650);
    }

    @Override
    public void hide() {
        super.hide();
        disposeTextures();
    }

    private void disposeTextures() {
        if (bgTex != null) bgTex.dispose();
        if (frameTex != null) frameTex.dispose();
        if (lockTex != null) lockTex.dispose();
        if (electroSlimeTex != null) electroSlimeTex.dispose();
        bgTex = null;
        frameTex = null;
        lockTex = null;
        electroSlimeTex = null;
    }

    private Table makeFusionCell(Skin skin, UiFactory ui, boolean unlocked, String fusionId) {
        Table cell = new Table();
        cell.setBackground(skin.newDrawable("white", new Color(0.15f, 0.16f, 0.2f, 1f)));
        cell.defaults().pad(6);

        Table frame = new Table();
        Drawable frameDrawable = loadDrawable(FRAME_PATH, () -> frameTex, t -> frameTex = t);
        if (frameDrawable != null) frame.setBackground(frameDrawable);

        Image icon;
        if (unlocked) {
            Drawable fusionDrawable = null;
            if ("FUSION_1".equals(fusionId)) {
                fusionDrawable = loadDrawable(FUSION_ELECTROSLIME_PATH, () -> electroSlimeTex, t -> electroSlimeTex = t);
            }
            if (fusionDrawable != null) {
                icon = new Image(fusionDrawable);
            } else {
                icon = Scene2dPlaceholders.coloredSquare(skin, new Color(0.35f, 0.65f, 0.5f, 1f));
            }
        } else {
            Drawable lockDrawable = loadDrawable(LOCK_PATH, () -> lockTex, t -> lockTex = t);
            if (lockDrawable != null) {
                icon = new Image(lockDrawable);
                icon.setColor(0.55f, 0.55f, 0.55f, 1f);
            } else {
                icon = Scene2dPlaceholders.coloredSquare(skin, new Color(0.35f, 0.35f, 0.35f, 1f));
            }
        }

        frame.add(icon).size(92).pad(10);

        cell.add(frame).size(120).row();
        cell.add(ui.label(unlocked ? fusionId : "Locked")).padTop(4);
        return cell;
    }

    private interface TexGetter {
        Texture get();
    }

    private interface TexSetter {
        void set(Texture texture);
    }

    private Drawable loadDrawable(String path, TexGetter getter, TexSetter setter) {
        if (path == null || !com.badlogic.gdx.Gdx.files.internal(path).exists()) {
            return null;
        }

        Texture t = getter.get();
        if (t == null) {
            t = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            setter.set(t);
        }
        return new TextureRegionDrawable(new TextureRegion(t));
    }
}
