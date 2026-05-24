package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.PlaceholderSimpleScreenView;

public final class CollectionsScreen extends BaseScreen {
    public CollectionsScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        PlaceholderSimpleScreenView view = new PlaceholderSimpleScreenView(context, "Collections", "TODO: unlocked fusions list");
        stage.addActor(view.getRoot());
    }
}

