package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.MainMenuView;

public final class MainMenuScreen extends BaseScreen {
    private MainMenuView view;

    public MainMenuScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        view = new MainMenuView(context);
        view.setPlayListener(() -> game.setScreen(new LabGameScreen(game, context, context.saves.get().currentLevel)));
        stage.addActor(view.getRoot());
    }
}
