package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.ui.UiFactory;

public final class LevelCompleteView {
    private final Table root;
    private Runnable continueListener;

    public LevelCompleteView(GameContext context) {
        Skin skin = PlaceholderSkinFactory.create();
        UiFactory ui = new UiFactory(skin);

        root = ui.panel();
        root.setFillParent(true);

        TextButton cont = ui.textButton("Back to Menu");
        cont.addListener(e -> {
            if (continueListener != null) continueListener.run();
            return true;
        });

        root.add(ui.label("Level Complete")).pad(20).row();
        root.add(cont).width(260).height(54).pad(20);
    }

    public Actor getRoot() {
        return root;
    }

    public void setContinueListener(Runnable continueListener) {
        this.continueListener = continueListener;
    }
}

