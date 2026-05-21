package com.splicelab.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.assets.PlaceholderSkinFactory;

public final class LoadingScreen extends BaseScreen {
    private Skin skin;
    private ProgressBar bar;

    public LoadingScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        context.assets.loadMinimal();
        skin = PlaceholderSkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);

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
            game.setScreen(new MainMenuScreen(game, context));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (skin != null) skin.dispose();
    }
}

