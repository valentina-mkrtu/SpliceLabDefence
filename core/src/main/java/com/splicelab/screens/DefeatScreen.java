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
        int consolationDna = 0;
        if (levelOpt.isPresent()) {
            int baseDna = levelOpt.get().rewards.coins();
            float mult = Math.max(0f, context.config.defeatCoinsMultiplier);
            consolationDna = Math.max(0, Math.round(baseDna * mult));
            if (consolationDna > 0) context.economy.add(com.splicelab.model.CurrencyType.DNA, consolationDna);
            // 0 CRY by design.
        }

        DefeatView view = new DefeatView(context, consolationDna);
        view.setRetryListener(() -> game.setScreen(new LabGameScreen(game, context, context.saves.get().currentLevel)));
        view.setExitListener(() -> game.setScreen(new MainLobbyScreen(game, context)));
        stage.addActor(view.getRoot());
    }
}
