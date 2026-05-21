package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.LevelCompleteView;

public final class LevelCompleteScreen extends BaseScreen {
    public LevelCompleteScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        LevelCompleteView view = new LevelCompleteView(context);
        view.setContinueListener(() -> game.setScreen(new MainMenuScreen(game, context)));
        stage.addActor(view.getRoot());
    }
}

