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
    private static final String[] FUSION_ICON_PATHS = new String[] {
            "art/fusions/criofungy.png",
            "art/fusions/criomech.png",
            "art/fusions/crioslime.png",
            "art/fusions/crystalfungy.png",
            "art/fusions/crystalmech.png",
            "art/fusions/crystalslime.png",
            "art/fusions/electrofungy.png",
            "art/fusions/electroslime.png",
            "art/fusions/mechbot.png",
            "art/fusions/nanofungy.png",
            "art/fusions/nanomechbot.png",
            "art/fusions/nanoslime.png",
            "art/fusions/radioactivefungy.png",
            "art/fusions/radioactivemech.png",
            "art/fusions/radioactiveslime.png",
            "art/fusions/toxicfungy.png",
            "art/fusions/toxicmech.png",
            "art/fusions/toxicslime.png"
    };

    private Texture bgTex;
    private Image bgImage;
    private Texture frameTex;
    private Texture lockTex;
    private Texture[] fusionTextures;
    private DialogCloseImageFactory.CloseImage closeButton;

    public CollectionsDialog(Skin skin, GameContext context) {
        super("Collections", skin);

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        createBackgroundIfNeeded(context);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table topRight = new Table();
        topRight.setFillParent(true);
        closeButton = DialogCloseImageFactory.create();
        Image closeBtn = closeButton.image;
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        topRight.top().right();
        topRight.add(closeBtn).size(48).padTop(38).padRight(58);
        addActor(topRight);

        Table grid = new Table();
        grid.defaults().pad(14);

        int columns = 2;
        for (int i = 0; i < 18; i++) {
            String fusionId = "FUSION_" + (i + 1);
            boolean unlocked = context.fusionUnlocks.isUnlocked(fusionId);
            // Slightly smaller cards so they stay inside the window frame.
            grid.add(makeFusionCell(skin, ui, unlocked, fusionId)).width(130);
            if ((i + 1) % columns == 0) grid.row();
        }

        ScrollPane scroll = new ScrollPane(grid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollbarsVisible(false);
        scroll.setOverscroll(false, false);
        scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
        if (scroll.getStyle() != null) {
            scroll.getStyle().background = null;
            scroll.getStyle().corner = null;
        }
        if (scroll.getStyle() != null) {
            scroll.getStyle().hScroll = null;
            scroll.getStyle().hScrollKnob = null;
            scroll.getStyle().vScroll = null;
            scroll.getStyle().vScrollKnob = null;
        }

        // Hard-kill any scrollbar rendering regardless of skin.
        scroll.setScrollBarPositions(false, false);
        scroll.setScrollbarsOnTop(false);

        // Match Account window sizing so all windows align.
        // Reduce viewport height a bit so the last row never spills.
        getContentTable().add(scroll).width(420).height(470).pad(22);
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
        bgImage.setPosition(20f, 200f);
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded(null);
        if (bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        bgImage.setZIndex(Math.max(0, getZIndex() - 1));
        bgImage.setColor(1f, 1f, 1f, 1f);
        syncBackground();
    }

    private void createBackgroundIfNeeded(GameContext context) {
        if (bgImage != null) return;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(BG_PATH);
        if (!bgFile.exists()) {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
            return;
        }

        Texture texture = null;
        if (context != null && context.assets != null) {
            texture = context.assets.getTexture(BG_PATH);
        }
        if (texture == null) {
            texture = new Texture(bgFile);
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bgTex = texture;
        bgImage = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bgImage.setFillParent(false);
        bgImage.setColor(1f, 1f, 1f, 1f);
        setColor(1f, 1f, 1f, 1f);
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
        // bgTex may be owned by AssetManager; don't dispose here.
        if (frameTex != null) frameTex.dispose();
        if (lockTex != null) lockTex.dispose();
        if (fusionTextures != null) {
            for (Texture t : fusionTextures) {
                if (t != null) t.dispose();
            }
        }
        bgTex = null;
        frameTex = null;
        lockTex = null;
        fusionTextures = null;
    }

    private Drawable loadFusionDrawable(String fusionId) {
        if (fusionId == null || !fusionId.startsWith("FUSION_")) return null;
        int index;
        try {
            index = Integer.parseInt(fusionId.substring("FUSION_".length())) - 1;
        } catch (Exception ignored) {
            return null;
        }
        if (index < 0 || index >= FUSION_ICON_PATHS.length) return null;

        if (fusionTextures == null) fusionTextures = new Texture[FUSION_ICON_PATHS.length];
        String path = FUSION_ICON_PATHS[index];
        if (path == null || !com.badlogic.gdx.Gdx.files.internal(path).exists()) return null;

        Texture t = fusionTextures[index];
        if (t == null) {
            t = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            fusionTextures[index] = t;
        }
        return new TextureRegionDrawable(new TextureRegion(t));
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
            Drawable fusionDrawable = loadFusionDrawable(fusionId);
            icon = fusionDrawable != null
                    ? new Image(fusionDrawable)
                    : Scene2dPlaceholders.coloredSquare(skin, new Color(0.35f, 0.65f, 0.5f, 1f));
        } else {
            Drawable lockDrawable = loadDrawable(LOCK_PATH, () -> lockTex, t -> lockTex = t);
            if (lockDrawable != null) {
                icon = new Image(lockDrawable);
                icon.setColor(0.55f, 0.55f, 0.55f, 1f);
            } else {
                icon = Scene2dPlaceholders.coloredSquare(skin, new Color(0.35f, 0.35f, 0.35f, 1f));
            }
        }

        // Scale down the card contents ~30%.
        frame.add(icon).size(64).pad(7);

        cell.add(frame).size(84).row();
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
