package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.model.level.LevelRewardSummary;
import com.splicelab.ui.UiFactory;

public final class LevelCompleteView {
    private final Table root;
    private Runnable claimListener;
    private Runnable doubleListener;

    public LevelCompleteView(GameContext context, LevelRewardSummary rewards) {
        Skin skin = PlaceholderSkinFactory.create();
        UiFactory ui = new UiFactory(skin, context.audio);

        root = ui.panel();
        root.setFillParent(true);

        int coins = rewards == null ? 0 : rewards.coins();
        int dna = rewards == null ? 0 : rewards.dna();

        Table earned = new Table();
        earned.defaults().pad(10);
        earned.add(ui.label("Coins: " + coins)).row();
        earned.add(ui.label("DNA: " + dna)).row();

        TextButton claimBtn = ui.textButton("Claim");
        claimBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (claimListener != null) claimListener.run();
            }
        });

        TextButton doubleBtn = ui.textButton("Double (Ad)");
        doubleBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (doubleListener != null) doubleListener.run();
            }
        });

        root.add(ui.label("Level Complete")).pad(20).row();
        root.add(earned).pad(10).row();
        root.add(claimBtn).width(260).height(54).pad(10).row();
        root.add(doubleBtn).width(260).height(54).pad(10);
    }

    public Actor getRoot() {
        return root;
    }

    public void setClaimListener(Runnable claimListener) {
        this.claimListener = claimListener;
    }

    public void setDoubleListener(Runnable doubleListener) {
        this.doubleListener = doubleListener;
    }
}
