package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

    public AccountDialog(Skin skin, GameContext context) {
        super("Account", skin);

        if (com.badlogic.gdx.Gdx.files.internal(BG_PATH).exists()) {
            bgTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(BG_PATH));
            bgTex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            var bg = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex));
            getContentTable().setBackground(bg);
            getButtonTable().setBackground(bg);
        }

        UiFactory ui = new UiFactory(skin, context.audio);
        SaveData save = context.saves.get();

        Table content = new Table();
        content.defaults().pad(8);

        Image pfp = Scene2dPlaceholders.coloredSquare(skin, new Color(0.25f, 0.3f, 0.45f, 1f));
        content.add(pfp).size(72).row();

        content.add(ui.label(save.playerName)).row();
        content.add(ui.label("Level " + save.playerLevel)).row();

        content.add(ui.label("Day Streak: " + save.dayStreak)).row();
        content.add(ui.label("Total Fusions: " + save.totalFusionsUnlocked)).row();

        getContentTable().add(content).width(440).height(520).pad(10);
        TextButton close = ui.textButton("Close");
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
        setResizable(false);
        pad(12);

        // Match Collections/Entities window size.
        setSize(480, 650);
    }

    @Override
    public void hide() {
        super.hide();
        if (bgTex != null) bgTex.dispose();
        bgTex = null;
    }
}
