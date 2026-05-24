package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.PlaceholderSimpleScreenView;

public final class EntitiesScreen extends BaseScreen {
    public EntitiesScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        PlaceholderSimpleScreenView view = new PlaceholderSimpleScreenView(context, "Entities", "TODO: entity + item cards");
        stage.addActor(view.getRoot());
    }
}

