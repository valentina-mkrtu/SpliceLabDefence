package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.data.SaveData;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class AccountDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseButtonFactory.CloseButton closeButton;

    public AccountDialog(Skin skin, GameContext context) {
        super("Account", skin);

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getContentTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getButtonTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(BG_PATH);
        if (bgFile.exists()) {
            bgTex = new com.badlogic.gdx.graphics.Texture(bgFile);
            bgTex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            bgImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex)));
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
        SaveData save = context.saves.get();

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

        Table content = new Table();
        content.defaults().pad(8);

        Image pfp = Scene2dPlaceholders.coloredSquare(skin, new Color(0.25f, 0.3f, 0.45f, 1f));
        content.add(pfp).size(72).row();

        content.add(ui.label(save.playerName)).row();
        content.add(ui.label("Level " + save.playerLevel)).row();

        content.add(ui.label("Day Streak: " + save.dayStreak)).row();
        content.add(ui.label("Total Fusions: " + save.totalFusionsUnlocked)).row();

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
        bgImage = null;
        if (bgTex != null) bgTex.dispose();
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }
}
