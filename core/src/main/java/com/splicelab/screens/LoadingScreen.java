package com.splicelab.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.ui.Scene2dPlaceholders;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class LoadingScreen extends BaseScreen {
    private static final String LOADING_BG_PATH = "art/backgrounds/loading.png";

    private Skin skin;
    private ProgressBar bar;
    private Texture loadingBgTex;

    public LoadingScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        context.assets.loadMinimal();
        context.assets.loadUi();
        skin = PlaceholderSkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);

        Image bg;
        var bgFile = com.badlogic.gdx.Gdx.files.internal(LOADING_BG_PATH);
        if (bgFile.exists()) {
            loadingBgTex = new Texture(bgFile);
            loadingBgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bg = new Image(new TextureRegionDrawable(new TextureRegion(loadingBgTex)));
        } else {
            bg = Scene2dPlaceholders.coloredSquare(skin, new Color(0.06f, 0.06f, 0.08f, 1f));
        }
        bg.setFillParent(true);
        stage.addActor(bg);

        Label label = new Label("Loading...", new Label.LabelStyle(skin.getFont("default-font"), null));
        bar = new ProgressBar(0f, 1f, 0.01f, false, skin);
        bar.setAnimateDuration(0.1f);

        root.add(label).pad(10).row();
        root.add(bar).width(320).height(16).pad(10);
        stage.addActor(root);
    }

    @Override
    protected void update(float delta) {
        boolean done = context.assets.update();
        if (bar != null) {
            bar.setValue(context.assets.getProgress());
        }
        if (done) {
            game.setScreen(new MainLobbyScreen(game, context));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (skin != null) skin.dispose();
        if (loadingBgTex != null) loadingBgTex.dispose();
    }
}
