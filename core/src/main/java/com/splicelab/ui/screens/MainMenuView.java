package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;

public final class MainMenuView {
    private final Table root;
    private final Skin skin;
    private Runnable playListener;
    private Runnable accountListener;
    private Runnable entitiesListener;
    private Runnable collectionsListener;

    public MainMenuView(GameContext context) {
        skin = context.skin;
        UiFactory ui = new UiFactory(skin, context.audio);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        TextButton play = ui.textButton("Play");
        play.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (playListener != null) playListener.run();
            }
        });

        root.add(ui.label("Splice Lab")).pad(20).row();
        root.add(play).width(240).height(54).pad(20).row();

        Table nav = new Table();
        nav.setBackground(skin.newDrawable("white", UiConstants.PANEL_DARK));

        TextButton accountBtn = ui.textButton("Account");
        TextButton entitiesBtn = ui.textButton("Entities");
        TextButton collectionsBtn = ui.textButton("Collections");

        accountBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (accountListener != null) accountListener.run();
            }
        });
        entitiesBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (entitiesListener != null) entitiesListener.run();
            }
        });
        collectionsBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (collectionsListener != null) collectionsListener.run();
            }
        });

        nav.add(accountBtn).height(54).pad(10).expandX();
        nav.add(entitiesBtn).height(54).pad(10).expandX();
        nav.add(collectionsBtn).height(54).pad(10).expandX();

        root.add().expandY().row();
        root.add(nav).expandX().fillX().height(84);
    }

    public Actor getRoot() {
        return root;
    }

    public void setPlayListener(Runnable playListener) {
        this.playListener = playListener;
    }

    public void setAccountListener(Runnable accountListener) {
        this.accountListener = accountListener;
    }

    public void setEntitiesListener(Runnable entitiesListener) {
        this.entitiesListener = entitiesListener;
    }

    public void setCollectionsListener(Runnable collectionsListener) {
        this.collectionsListener = collectionsListener;
    }

    public void dispose() {
    }
}
