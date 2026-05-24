package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.data.SaveData;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class AccountDialog extends Dialog {
    public AccountDialog(Skin skin, GameContext context) {
        super("Account", skin);

        UiFactory ui = new UiFactory(skin, context.audio);
        SaveData save = context.saves.get();

        Table content = getContentTable();
        content.defaults().pad(8);

        Image pfp = Scene2dPlaceholders.coloredSquare(skin, new Color(0.25f, 0.3f, 0.45f, 1f));
        content.add(pfp).size(72).row();

        content.add(ui.label(save.playerName)).row();
        content.add(ui.label("Level " + save.playerLevel)).row();

        content.add(ui.label("Day Streak: " + save.dayStreak)).row();
        content.add(ui.label("Total Fusions: " + save.totalFusionsUnlocked)).row();

        button("Close");

        setModal(true);
        setMovable(false);
        setResizable(false);
        pad(12);
    }
}
