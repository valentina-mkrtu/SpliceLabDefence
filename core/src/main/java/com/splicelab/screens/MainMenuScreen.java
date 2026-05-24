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
        // Prototype safety: always start at Level 1 from main menu.
        view.setPlayListener(() -> game.setScreen(new MainLobbyScreen(game, context)));
        view.setAccountListener(() -> game.setScreen(new AccountScreen(game, context)));
        view.setEntitiesListener(() -> game.setScreen(new EntitiesScreen(game, context)));
        view.setCollectionsListener(() -> game.setScreen(new CollectionsScreen(game, context)));
        stage.addActor(view.getRoot());
    }

    @Override
    public void dispose() {
        super.dispose();
        if (view != null) view.dispose();
    }
}
