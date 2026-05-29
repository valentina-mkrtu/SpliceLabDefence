package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.UiStyle;

public final class DefeatView {
    private static final String LEVEL_END_BG_PATH = "art/backgrounds/levelend.png";

    private static final float CARD_WIDTH_PX = 480f;

    private final Stack root;
    private final Skin skin;
    private Runnable retryListener;
    private Runnable exitListener;

    public DefeatView(GameContext context, int consolationDna) {
        skin = context.skin;
        UiFactory ui = new UiFactory(skin, context.audio);

        Drawable levelEndBg = context.assets.getDrawable(LEVEL_END_BG_PATH);
        root = new Stack();
        root.setFillParent(true);

        if (levelEndBg != null) {
            Image bg = new Image(levelEndBg);
            bg.setScaling(Scaling.fill);
            root.add(bg);
        }

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(UiStyle.dimBackground(skin));
        root.add(overlay);

        Table card = UiStyle.cardPanel(skin);
        Drawable cardBg = context.assets.getDrawable("art/backgrounds/menuwindowbg.png");
        if (cardBg != null) card.setBackground(cardBg);
        card.defaults().pad(10).expandX().fillX();

        TextButton retry = ui.textButton("Retry");
        retry.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (retryListener != null) retryListener.run();
            }
        });

        TextButton exit = ui.textButton("Exit to Lobby");
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (exitListener != null) exitListener.run();
            }
        });

        var header = ui.label("DEFEAT");
        header.setFontScale(1.6f);
        header.setColor(UiStyle.DEFEAT_ACCENT);

        Table consolation = new Table();
        consolation.defaults().pad(4);
        consolation.add(ui.label("Consolation: +" + Math.max(0, consolationDna) + " DNA"));

        card.add(header).padTop(22).padBottom(18).row();
        card.add(consolation).padBottom(18).row();
        card.add(retry).width(360).height(56).padBottom(10).row();
        card.add(exit).width(360).height(56).padBottom(22);

        overlay.add(card).width(CARD_WIDTH_PX).pad(20);
    }

    public Actor getRoot() {
        return root;
    }

    public void setRetryListener(Runnable retryListener) {
        this.retryListener = retryListener;
    }

    public void setExitListener(Runnable exitListener) {
        this.exitListener = exitListener;
    }

    public void dispose() {
    }
}
