package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.splicelab.app.GameContext;
import com.splicelab.model.level.LevelRewardSummary;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.UiStyle;

public final class LevelCompleteView {
    private static final String LEVEL_END_BG_PATH = "art/backgrounds/mainbg.png";
    private static final String CLAIM_BUTTON_PATH = "art/icons/claim.png";
    private static final String DOUBLE_CLAIM_BUTTON_PATH = "art/icons/doubleclaim.png";

    private static final float CARD_WIDTH_PX = 480f;
    private static final float BUTTON_WIDTH_PX = 360f;
    private static final float BUTTON_HEIGHT_PX = 120f;

    private final Stack root;
    private final Skin skin;
    private Runnable claimListener;
    private Runnable doubleListener;

    public LevelCompleteView(GameContext context, LevelRewardSummary rewards) {
        skin = context.skin;
        UiFactory ui = new UiFactory(skin, context.audio);

        root = new Stack();
        root.setFillParent(true);

        Drawable levelEndBg = context.assets.getDrawable(LEVEL_END_BG_PATH);
        if (levelEndBg != null) {
            Image bg = new Image(levelEndBg);
            bg.setScaling(Scaling.fill);
            root.add(bg);
        }

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(UiStyle.dimBackground(skin));
        root.add(overlay);

        int dna = rewards == null ? 0 : rewards.dna();
        int cry = rewards == null ? 0 : rewards.cry();

        Table card = UiStyle.cardPanel(skin);
        Drawable cardBg = context.assets.getDrawable("art/backgrounds/menuwindowbg.png");
        if (cardBg != null) card.setBackground(cardBg);
        card.defaults().pad(10).expandX().fillX();

        var header = ui.label("LEVEL COMPLETE");
        header.setFontScale(1.6f);
        header.setAlignment(com.badlogic.gdx.utils.Align.center);

        Table earned = new Table();
        earned.defaults().pad(8);
        Drawable dnaIcon = context.assets.getDrawable("art/icons/dna.png");
        Drawable cryIcon = context.assets.getDrawable("art/icons/cry.png");
        if (dnaIcon != null) earned.add(new Image(dnaIcon)).size(26).padRight(6);
        earned.add(ui.label("+" + dna)).padRight(18);
        if (cryIcon != null) earned.add(new Image(cryIcon)).size(26).padRight(6);
        earned.add(ui.label("+" + cry));

        Actor claimBtn = makeRewardButton(context, CLAIM_BUTTON_PATH, "Claim", () -> {
            if (claimListener != null) claimListener.run();
        });

        Actor doubleBtn = makeRewardButton(context, DOUBLE_CLAIM_BUTTON_PATH, "2× (Watch Ad)", () -> {
            if (doubleListener != null) doubleListener.run();
        });

        card.add(header).padTop(22).padBottom(18).row();
        card.add(earned).padBottom(22).row();
        card.add(claimBtn).width(BUTTON_WIDTH_PX).height(BUTTON_HEIGHT_PX).padBottom(12).row();
        card.add(doubleBtn).width(BUTTON_WIDTH_PX).height(BUTTON_HEIGHT_PX).padBottom(22);

        overlay.add(card).width(CARD_WIDTH_PX).pad(20);
    }

    private Actor makeRewardButton(GameContext context, String texturePath, String fallbackText, Runnable onClick) {
        Drawable icon = context.assets.getDrawable(texturePath);
        if (icon == null) {
            UiFactory ui = new UiFactory(skin, context.audio);
            TextButton btn = ui.textButton(fallbackText);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (onClick != null) onClick.run();
                }
            });
            return btn;
        }

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));
        style.down = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));
        style.imageUp = icon;
        style.imageDown = icon;

        ImageButton btn = new ImageButton(style);
        btn.getImageCell().grow();
        btn.getImage().setScaling(Scaling.fit);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (context.audio != null) context.audio.playButtonClick();
                if (onClick != null) onClick.run();
            }
        });
        return btn;
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

    public void dispose() {
    }
}
