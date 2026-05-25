package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;

public final class LevelMapDialog extends Dialog {
    public interface LevelSelectListener {
        void onSelectLevel(int levelNumber);
    }

    // Debug flag: keep OFF for now.
    // When enabled, only cleared levels + next available level are selectable.
    private static final boolean ENABLE_LEVEL_LOCK = false;

    private static final String LEVEL_BG_TEXTURE_PATH = "art/backgrounds/levels.png";
    private static final String WINDOW_BG_TEXTURE_PATH = "art/backgrounds/levelsbg.png";
    private static final Color LEVEL_TEXT_COLOR = new Color(0.07f, 0.16f, 0.45f, 1f);
    private static final float LEVEL_WIDGET_SCALE = 0.70f;
    private static final int LEVEL_COLUMNS = 2;

    private static final float CONTENT_PAD = 10f;

    private Texture levelBgTexture;
    private Texture windowBgTexture;
    private BitmapFont levelFont;

    public LevelMapDialog(Skin skin, GameContext context, LevelSelectListener listener) {
        super("Map", skin);

        UiFactory ui = new UiFactory(skin, context.audio);

        levelBgTexture = null;
        windowBgTexture = null;
        TextureRegionDrawable levelBgDrawable = null;
        if (com.badlogic.gdx.Gdx.files.internal(LEVEL_BG_TEXTURE_PATH).exists()) {
            levelBgTexture = new Texture(com.badlogic.gdx.Gdx.files.internal(LEVEL_BG_TEXTURE_PATH));
            levelBgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            levelBgDrawable = new TextureRegionDrawable(new TextureRegion(levelBgTexture));
        }

        if (com.badlogic.gdx.Gdx.files.internal(WINDOW_BG_TEXTURE_PATH).exists()) {
            windowBgTexture = new Texture(com.badlogic.gdx.Gdx.files.internal(WINDOW_BG_TEXTURE_PATH));
            windowBgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            setBackground(new TextureRegionDrawable(new TextureRegion(windowBgTexture)));
        }

        BitmapFont baseFont = skin.getFont("default-font");
        levelFont = new BitmapFont(baseFont.getData(), baseFont.getRegion(), baseFont.usesIntegerPositions());
        levelFont.getData().setScale(1.15f * LEVEL_WIDGET_SCALE);
        Label.LabelStyle levelLabelStyle = new Label.LabelStyle(levelFont, LEVEL_TEXT_COLOR);

        Table list = new Table();
        list.setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        list.defaults().pad(8 * LEVEL_WIDGET_SCALE).expand().fill();

        int totalLevels = 50;
        for (int level = 1; level <= totalLevels; level++) {
            boolean cleared = context.saves.get().completedLevels.contains(level);

            boolean unlocked = true;
            if (ENABLE_LEVEL_LOCK) {
                // Allow replay of cleared levels, plus the next uncleared level.
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
        scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
        if (scroll.getStyle() != null) scroll.getStyle().background = null;
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollbarsVisible(true);
        scroll.setOverscroll(false, false);
        scroll.setForceScroll(false, true);
        scroll.setFlickScroll(true);

        // Do not hardcode size here.
        // MainLobbyScreen sizes the dialog to the viewport, so we let the ScrollPane fill it.
        getContentTable().add(scroll).grow().pad(CONTENT_PAD);
        button("Close");

        // Ensure the Close button doesn't inherit the last-used background.
        if (getButtonTable().getCells().size > 0) {
            if (getButtonTable().getCells().first().getActor() instanceof TextButton closeButton) {
                closeButton.getStyle().up = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 1f));
                closeButton.getStyle().down = skin.newDrawable("white", new Color(0.16f, 0.16f, 0.16f, 1f));
            }
        }

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(12);
    }

    @Override
    public Dialog show(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        Dialog shown = super.show(stage);
        pack();
        return shown;
    }

    @Override
    public void hide() {
        super.hide();
        disposeAssets();
    }

    @Override
    public boolean remove() {
        boolean removed = super.remove();
        if (removed) disposeAssets();
        return removed;
    }

    private void disposeAssets() {
        if (levelBgTexture != null) {
            levelBgTexture.dispose();
            levelBgTexture = null;
        }
        if (windowBgTexture != null) {
            windowBgTexture.dispose();
            windowBgTexture = null;
        }
        if (levelFont != null) {
            levelFont.dispose();
            levelFont = null;
        }
    }
}
