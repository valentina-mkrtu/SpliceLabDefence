package com.splicelab.ui.windows;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.UiStyle;

public final class PauseDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/settingsbg.png";

    public PauseDialog(Skin skin, GameContext ctx, Runnable onResume, Runnable onExit) {
        super("Paused", skin);

        setModal(true);
        setMovable(false);
        setResizable(false);

        if (getStyle() != null) {
            Drawable bg = ctx != null && ctx.assets != null ? ctx.assets.getDrawable(BG_PATH) : null;
            getStyle().background = bg != null ? bg : skin.newDrawable("white", UiStyle.PANEL_DARK);
        }

        UiFactory ui = new UiFactory(skin, ctx.audio);

        Table content = new Table();
        content.defaults().pad(10).expandX().fillX();

        TextButton resumeBtn = ui.textButton("Resume");
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                if (onResume != null) onResume.run();
            }
        });

        TextButton exitBtn = ui.textButton("Exit to Lobby");
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                if (onExit != null) onExit.run();
            }
        });

        content.add(resumeBtn).height(54).row();
        content.add(exitBtn).height(54).row();

        getContentTable().add(content).width(320).pad(18);
        getButtonTable().clearChildren();
        pack();
    }
}
