package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;

public final class MainMenuView {
    private final Table root;
    private Runnable playListener;

    public MainMenuView(GameContext context) {
        Skin skin = PlaceholderSkinFactory.create();
        UiFactory ui = new UiFactory(skin);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        TextButton play = ui.textButton("Play");
        play.addListener(e -> {
            if (playListener != null) playListener.run();
            return true;
        });

        root.add(ui.label("Splice Lab")).pad(20).row();
        root.add(play).width(240).height(54).pad(20);
    }

    public Actor getRoot() {
        return root;
    }

    public void setPlayListener(Runnable playListener) {
        this.playListener = playListener;
    }
}

