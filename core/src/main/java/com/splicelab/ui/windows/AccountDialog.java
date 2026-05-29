package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.data.SaveData;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class AccountDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";
    private static final String PFP_ICON_PATH = "art/icons/pfp.png";

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseImageFactory.CloseImage closeButton;

    public AccountDialog(Skin skin, GameContext context) {
        super("Account", skin);

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getContentTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getButtonTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        createBackgroundIfNeeded(context);

        UiFactory ui = new UiFactory(skin, context.audio);
        SaveData save = context.saves.get();

        Table topRight = new Table();
        topRight.setFillParent(true);
        closeButton = DialogCloseImageFactory.create(context.assets);
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

        Table content = new Table();
        content.defaults().pad(8);

        com.badlogic.gdx.files.FileHandle pfpFile = com.badlogic.gdx.Gdx.files.internal(PFP_ICON_PATH);
        Image pfp;
        if (pfpFile.exists()) {
            var tex = new com.badlogic.gdx.graphics.Texture(pfpFile);
            tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            pfp = new Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(tex)));
        } else {
            pfp = Scene2dPlaceholders.coloredSquare(skin, new Color(0.25f, 0.3f, 0.45f, 1f));
        }
        content.add(pfp).size(72).row();

        Color darkBlue = new Color(0.05f, 0.13f, 0.28f, 1f);
        Label nameLabel = ui.label(save.playerName);
        nameLabel.setColor(darkBlue);
        nameLabel.setFontScale(1.15f);
        content.add(nameLabel).row();

        Label level = ui.label("Level " + save.playerLevel);
        level.setColor(darkBlue);
        level.setFontScale(1.1f);
        content.add(level).row();

        Label streak = ui.label("Day Streak: " + save.dayStreak);
        streak.setColor(darkBlue);
        streak.setFontScale(1.1f);
        content.add(streak).row();

        Label fusions = ui.label("Total Fusions: " + save.totalFusionsUnlocked);
        fusions.setColor(darkBlue);
        fusions.setFontScale(1.1f);
        content.add(fusions).row();

        // Leave margin so background frame is visible.
        getContentTable().add(content).width(420).height(500).pad(22);
        getButtonTable().clearChildren();

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        setResizable(false);
        pad(18);

        setColor(1f, 1f, 1f, 1f);

        normalizeWindowSize();

        if (bgImage != null) {
            // Match dialog size; background sits behind tables.
            bgImage.setSize(getWidth(), getHeight());
            bgImage.setPosition(0f, 0f);
        }

        // Keep style background transparent; we render bg via bgImage actor.
        if (getStyle() != null) getStyle().background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));

        // Keep default dialog style background.
    }

    private void normalizeWindowSize() {
        // Use a consistent size relative to the game's FitViewport (540x960).
        // This avoids huge/distorted dialogs on desktop.
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
        createBackgroundIfNeeded(context);
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

        com.badlogic.gdx.graphics.Texture texture = null;
        if (context != null && context.assets != null) {
            texture = context.assets.getTexture(BG_PATH);
        }
        if (texture == null) {
            bgImage = com.splicelab.ui.Scene2dPlaceholders.coloredSquare(
                    getSkin(),
                    new com.badlogic.gdx.graphics.Color(0.12f, 0.13f, 0.17f, 1f)
            );
            bgImage.setFillParent(false);
            bgImage.setColor(1f, 1f, 1f, 1f);
            return;
        }
        texture.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        bgTex = texture;
        bgImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex))
        );
        bgImage.setFillParent(false);
        bgImage.setColor(1f, 1f, 1f, 1f);
        setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void hide() {
        super.hide();
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        // bgTex may be owned by AssetManager; don't dispose here.
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }
}
