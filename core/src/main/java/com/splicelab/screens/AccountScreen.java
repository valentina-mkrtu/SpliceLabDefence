package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.ui.screens.PlaceholderSimpleScreenView;

public final class AccountScreen extends BaseScreen {
    public AccountScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void buildUi() {
        PlaceholderSimpleScreenView view = new PlaceholderSimpleScreenView(context, "Account", "TODO: profile, rename, pfp");
        stage.addActor(view.getRoot());
    }
}

