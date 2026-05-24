package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.ui.UiFactory;

public final class DefeatView {
    private final Table root;
    private Runnable retryListener;

    public DefeatView(GameContext context) {
        Skin skin = PlaceholderSkinFactory.create();
        UiFactory ui = new UiFactory(skin, context.audio);

        root = ui.panel();
        root.setFillParent(true);

        TextButton retry = ui.textButton("Retry");
        retry.addListener(e -> {
            if (retryListener != null) retryListener.run();
            return true;
        });

        root.add(ui.label("Defeat")).pad(20).row();
        root.add(retry).width(220).height(54).pad(20);
    }

    public Actor getRoot() {
        return root;
    }

    public void setRetryListener(Runnable retryListener) {
        this.retryListener = retryListener;
    }
}
