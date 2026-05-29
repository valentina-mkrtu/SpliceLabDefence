package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;

public final class PlaceholderSimpleScreenView {
    private static final String PFP_ICON_PATH = "art/icons/pfp.png";
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private final Table root;
    private final Skin skin;

    public PlaceholderSimpleScreenView(GameContext context, String title, String subtitle) {
        this.skin = context.skin;
        UiFactory ui = new UiFactory(skin, context.audio);

        root = new Table();
        root.setFillParent(true);
        var bgDrawable = context.assets.getDrawable(BG_PATH);
        if (bgDrawable != null) root.setBackground(bgDrawable);

        Table header = new Table();
        header.setBackground(skin.newDrawable("white", UiConstants.PANEL_DARK));

        var pfpDrawable = context.assets.getDrawable(PFP_ICON_PATH);
        Image pfp = pfpDrawable != null ? new Image(pfpDrawable) : new Image();
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
        // Textures are owned by AssetService — not disposed here. (T-2.2)
    }
}
