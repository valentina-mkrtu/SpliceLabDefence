package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.DefeatView;

public final class DefeatScreen extends BaseScreen {
    public DefeatScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        DefeatView view = new DefeatView(context);
        view.setRetryListener(() -> game.setScreen(new LabGameScreen(game, context, context.saves.get().currentLevel)));
        stage.addActor(view.getRoot());
    }
}

