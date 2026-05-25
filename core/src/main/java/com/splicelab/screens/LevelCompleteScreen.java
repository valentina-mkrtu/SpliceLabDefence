package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.model.level.LevelRewardSummary;
import com.splicelab.ui.screens.LevelCompleteView;

public final class LevelCompleteScreen extends BaseScreen {
    private final LevelRewardSummary rewards;

    public LevelCompleteScreen(SpliceLabGame game, GameContext context) {
        this(game, context, new LevelRewardSummary(0, 0));
    }

    public LevelCompleteScreen(SpliceLabGame game, GameContext context, LevelRewardSummary rewards) {
        super(game, context);
        this.rewards = rewards == null ? new LevelRewardSummary(0, 0) : rewards;
    }

    @Override
    protected void buildUi() {
        LevelCompleteView view = new LevelCompleteView(context, rewards);
        view.setClaimListener(() -> {
            context.economy.add(com.splicelab.model.CurrencyType.COINS, rewards.coins());
            context.economy.add(com.splicelab.model.CurrencyType.DNA, rewards.dna());
            game.setScreen(new MainLobbyScreen(game, context));
        });
        view.setDoubleListener(() -> {
            context.adRewardService.showRewardedAd(() -> {
                context.economy.add(com.splicelab.model.CurrencyType.COINS, rewards.coins() * 2);
                context.economy.add(com.splicelab.model.CurrencyType.DNA, rewards.dna() * 2);
                game.setScreen(new MainLobbyScreen(game, context));
            });
        });
        stage.addActor(view.getRoot());
    }
}
