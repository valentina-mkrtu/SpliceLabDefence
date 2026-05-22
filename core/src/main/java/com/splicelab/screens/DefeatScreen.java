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
        // Player declined ad revive (none offered yet), grant consolation payout.
        // Use the level the player just attempted.
        int lvl = context.saves.get().currentLevel;
        var levelOpt = context.levels.getLevel(lvl);
        if (levelOpt.isPresent()) {
            int baseCoins = levelOpt.get().rewards.coins();
            float mult = Math.max(0f, context.config.defeatCoinsMultiplier);
            int consolationCoins = Math.max(0, Math.round(baseCoins * mult));
            if (consolationCoins > 0) context.economy.add(com.splicelab.model.CurrencyType.COINS, consolationCoins);
            // 0 DNA by design.
        }

        DefeatView view = new DefeatView(context);
        view.setRetryListener(() -> game.setScreen(new LabGameScreen(game, context, context.saves.get().currentLevel)));
        stage.addActor(view.getRoot());
    }
}
