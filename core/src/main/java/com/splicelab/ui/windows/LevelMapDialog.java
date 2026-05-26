package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;

public final class LevelMapDialog extends Dialog {
    public interface LevelSelectListener {
        void onSelectLevel(int levelNumber);
    }

    private static final boolean ENABLE_LEVEL_LOCK = false;

    private static final String LEVEL_BG_TEXTURE_PATH = "art/backgrounds/levels.png";
    private static final String WINDOW_BG_TEXTURE_PATH = "art/backgrounds/levelsbg.png";
    private static final Color LEVEL_TEXT_COLOR = new Color(0.07f, 0.16f, 0.45f, 1f);

    private static final float LEVEL_WIDGET_SCALE = 0.70f;
    private static final int LEVEL_COLUMNS = 2;

    private static final float CONTENT_PAD = 22f;

    private Texture bgTex;
    private Image bgImage;
    private DialogCloseImageFactory.CloseImage closeButton;

    private final GameContext context;

    private Texture levelBgTexture;
    private BitmapFont levelFont;

    public LevelMapDialog(Skin skin, GameContext context, LevelSelectListener listener) {
        super("Map", skin);

        this.context = context;

        UiFactory ui = new UiFactory(skin, context.audio);

        // Take layout + background strategy from CollectionsDialog.
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);
        if (getStyle() != null) getStyle().background = null;

        createBackgroundIfNeeded();

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

        TextureRegionDrawable levelBgDrawable = null;
        if (com.badlogic.gdx.Gdx.files.internal(LEVEL_BG_TEXTURE_PATH).exists()) {
            levelBgTexture = new Texture(com.badlogic.gdx.Gdx.files.internal(LEVEL_BG_TEXTURE_PATH));
            levelBgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            levelBgDrawable = new TextureRegionDrawable(new TextureRegion(levelBgTexture));
        }

        BitmapFont baseFont = skin.getFont("default-font");
        levelFont = new BitmapFont(baseFont.getData(), baseFont.getRegion(), baseFont.usesIntegerPositions());
        levelFont.getData().setScale(1.15f * LEVEL_WIDGET_SCALE);
        Label.LabelStyle levelLabelStyle = new Label.LabelStyle(levelFont, LEVEL_TEXT_COLOR);

        Table list = new Table();
        list.setBackground((Drawable) null);
        // Reduce horizontal gap between columns.
        float padV = 8f * LEVEL_WIDGET_SCALE;
        float padH = padV * 0.5f;
        list.defaults().pad(padV, padH, padV, padH).expand().fill();

        int totalLevels = 50;
        for (int level = 1; level <= totalLevels; level++) {
            boolean cleared = context.saves.get().completedLevels.contains(level);

            boolean unlocked = true;
            if (ENABLE_LEVEL_LOCK) {
                unlocked = cleared || context.saves.get().completedLevels.contains(level - 1) || level == 1;
            }

            TextButton btn = ui.textButton("");
            btn.setDisabled(!unlocked);

            Label label = new Label("Level " + level, levelLabelStyle);
            label.setAlignment(Align.center);
            btn.clearChildren();
            btn.add(label).expand().fill();

            if (levelBgDrawable != null) {
                btn.getStyle().up = levelBgDrawable;
                btn.getStyle().down = levelBgDrawable;
                btn.getStyle().disabled = levelBgDrawable;
            } else {
                Color baseColor;
                if (!unlocked) baseColor = new Color(0.18f, 0.18f, 0.18f, 1f);
                else if (cleared) baseColor = new Color(0.25f, 0.65f, 0.35f, 1f);
                else baseColor = new Color(0.3f, 0.3f, 0.3f, 1f);

                btn.getStyle().up = skin.newDrawable("white", baseColor);
                btn.getStyle().down = skin.newDrawable(
                        "white",
                        !unlocked ? new Color(0.14f, 0.14f, 0.14f, 1f)
                                : (cleared ? new Color(0.2f, 0.55f, 0.3f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f))
                );
            }

            final int selected = level;
            btn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (!unlocked) return;
                    if (listener != null) listener.onSelectLevel(selected);
                    hide();
                }
            });

            float size = 90f * LEVEL_WIDGET_SCALE;
            list.add(btn).size(size, size);
            if (level % LEVEL_COLUMNS == 0) list.row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollbarsVisible(false);
        scroll.setOverscroll(false, false);
        scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
        if (scroll.getStyle() != null) {
            scroll.getStyle().background = null;
            scroll.getStyle().corner = null;
            scroll.getStyle().hScroll = null;
            scroll.getStyle().hScrollKnob = null;
            scroll.getStyle().vScroll = null;
            scroll.getStyle().vScrollKnob = null;
        }
        scroll.setScrollBarPositions(false, false);
        scroll.setScrollbarsOnTop(false);

        getContentTable().add(scroll).width(420).height(470).pad(CONTENT_PAD);
        getButtonTable().clearChildren();

        setModal(false);
        setMovable(false);
        pad(18);

        normalizeWindowSize();

        if (getStyle() != null) getStyle().background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));
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
        // Follow the dialog position (MainLobbyScreen centers dialogs).
        bgImage.setPosition(getX(), getY());
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded();
        if (bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        // Ensure the background sits behind this dialog even when the dialog
        // z-index changes after being added to the stage.
        bgImage.setZIndex(Math.max(0, getZIndex() - 1));
        bgImage.setColor(1f, 1f, 1f, 1f);
        syncBackground();
    }

    private void createBackgroundIfNeeded() {
        if (bgImage != null) return;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(WINDOW_BG_TEXTURE_PATH);
        if (!bgFile.exists()) {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
            return;
        }

        Texture texture = null;
        if (context != null && context.assets != null) {
            texture = context.assets.getTexture(WINDOW_BG_TEXTURE_PATH);
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
        // Keep textures alive; they are managed globally (AssetManager or per-dialog).
        // Just drop references to allow GC and avoid stale actors.
        bgTex = null;
        bgImage = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    @Override
    public boolean remove() {
        boolean removed = super.remove();
        if (removed) {
            bgTex = null;
            bgImage = null;
        }
        return removed;
    }
}
