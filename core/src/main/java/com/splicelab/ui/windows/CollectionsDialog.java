package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
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
    private Image bgImage;
    private Texture frameTex;
    private Texture lockTex;
    private Texture electroSlimeTex;
    private DialogCloseButtonFactory.CloseButton closeButton;

    public CollectionsDialog(Skin skin, GameContext context) {
        super("Collections", skin);

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(BG_PATH);
        if (bgFile.exists()) {
            bgTex = new Texture(bgFile);
            bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bgImage = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
            // Put the image behind content only (not full stage), sized to the dialog.
            bgImage.setFillParent(false);
            bgImage.setColor(1f, 1f, 1f, 1f);
            addActor(bgImage);
            bgImage.toBack();
            setColor(1f, 1f, 1f, 1f);
        } else {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
        }

        UiFactory ui = new UiFactory(skin, context.audio);

        Table topRight = new Table();
        topRight.setFillParent(true);
        closeButton = DialogCloseButtonFactory.create(skin);
        ImageButton closeBtn = closeButton.button;
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        topRight.top().right();
        topRight.add(closeBtn).size(56).pad(8);
        addActor(topRight);

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

        // Leave margin so background frame is visible.
        getContentTable().add(scroll).width(420).height(500).pad(22);
        getButtonTable().clearChildren();

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(18);

        setColor(1f, 1f, 1f, 1f);

        normalizeWindowSize();

        if (bgImage != null) {
            bgImage.setSize(getWidth(), getHeight());
            bgImage.setPosition(0f, 0f);
        }

        // Keep style background transparent; we render bg via bgImage actor.
        if (getStyle() != null) getStyle().background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));

        // Keep default dialog style background.
    }

    private void normalizeWindowSize() {
        float vw = 540f;
        float vh = 960f;
        float w = vw * 0.90f;
        float h = vh * 0.76f;
        setSize(w, h);
    }

    public void syncBackground() {
        if (bgImage == null) return;
        bgImage.setSize(getWidth(), getHeight());
        bgImage.setPosition(0f, 0f);
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null || bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        bgImage.toBack();
        bgImage.setColor(1f, 0f, 0f, 0.35f);
        syncBackground();
    }

    @Override
    public void hide() {
        super.hide();
        if (bgImage != null) bgImage.remove();
        disposeTextures();
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    private void disposeTextures() {
        if (bgImage != null) bgImage.remove();
        bgImage = null;
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
