package com.splicelab.ui.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;

public final class PlaceholderSimpleScreenView {
    private static final String PFP_ICON_PATH = "art/icons/pfp.png";

    private final Table root;
    private final Skin skin;
    private Texture pfpTexture;

    public PlaceholderSimpleScreenView(GameContext context, String title, String subtitle) {
        this.skin = PlaceholderSkinFactory.create();
        UiFactory ui = new UiFactory(skin, context.audio);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        Table header = new Table();
        header.setBackground(skin.newDrawable("white", UiConstants.PANEL_DARK));

        pfpTexture = new Texture(PFP_ICON_PATH);
        Image pfp = new Image(pfpTexture);
        header.add(pfp).size(44).pad(10);

        Table text = new Table();
        text.add(ui.label(title)).left().row();
        text.add(ui.smallLabel(subtitle)).left();
        header.add(text).left().expandX().fillX().pad(10);

        header.add(ui.smallLabel("TODO")).pad(10);

        root.add(header).expandX().fillX().row();
        root.add(ui.label("Placeholder screen")).pad(30).row();
        root.add(ui.smallLabel("Wire real UI later")).pad(10);
    }

    public Actor getRoot() {
        return root;
    }

    public void dispose() {
        skin.dispose();
        if (pfpTexture != null) pfpTexture.dispose();
    }
}
