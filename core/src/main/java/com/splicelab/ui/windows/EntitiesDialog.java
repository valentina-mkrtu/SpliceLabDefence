package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class EntitiesDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseButtonFactory.CloseButton closeButton;

    public EntitiesDialog(Skin skin, GameContext context) {
        super("Entities", skin);

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getContentTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        getButtonTable().setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        createBackgroundIfNeeded(context);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table topRight = new Table();
        topRight.setFillParent(true);
        closeButton = DialogCloseButtonFactory.create(skin);
        ImageButton closeBtn = closeButton.button;
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        topRight.top().right();
        topRight.add(closeBtn).size(48).padTop(38).padRight(58);
        addActor(topRight);

        Table list = new Table();
        list.defaults().pad(8).expandX().fillX();

        list.add(makeCard(skin, ui, "Slime", 20, 4, "Splits on hit")).row();
        list.add(makeCard(skin, ui, "Mech", 35, 6, "Armor: reduces damage")).row();
        list.add(makeCard(skin, ui, "Fungi", 22, 3, "Spore: poison over time")).row();
        list.add(makeCard(skin, ui, "Plasma", 18, 8, "Chain lightning")).row();
        list.add(makeCard(skin, ui, "Alien", 26, 5, "Mind shock: stun chance")).row();
        list.add(makeCard(skin, ui, "Egg", 12, 2, "Hatches after turns")).row();

        list.add(makeCard(skin, ui, "Battery", 0, 0, "+Energy on merge")).row();
        list.add(makeCard(skin, ui, "Toxic Waste", 0, 0, "Adds poison stacks")).row();
        list.add(makeCard(skin, ui, "Water Bottle", 0, 0, "Heals tube")).row();
        list.add(makeCard(skin, ui, "Lamp", 0, 0, "Reveals hidden bonus")).row();

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
        if (scroll.getStyle() != null) scroll.getStyle().background = null;

        // Leave margin so background frame is visible.
        getContentTable().add(scroll).width(340).height(410).pad(46).padLeft(64);
        getButtonTable().clearChildren();

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(18);

        setColor(1f, 1f, 1f, 1f);

        normalizeWindowSize();

        if (bgImage != null) {
            bgImage.setSize(getWidth(), getHeight());
            bgImage.setPosition(0f, 0f);
        }

        // Keep style background transparent; we render bg via bgImage actor.
        if (getStyle() != null) getStyle().background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));

        // Keep default dialog style background.
    }

    private void normalizeWindowSize() {
        float vw = 540f;
        float vh = 960f;
        float w = vw * 0.90f;
        float h = vh * 0.76f;
        setSize(w, h);
    }

    public void syncBackground() {
        if (bgImage == null) return;
        bgImage.setSize(getWidth(), getHeight());
        bgImage.setPosition(20f, 200f);
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded(null);
        if (bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        bgImage.setZIndex(Math.max(0, getZIndex() - 1));
        bgImage.setColor(1f, 1f, 1f, 1f);
        syncBackground();
    }

    private void createBackgroundIfNeeded(GameContext context) {
        if (bgImage != null) return;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(BG_PATH);
        if (!bgFile.exists()) {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
            return;
        }

        com.badlogic.gdx.graphics.Texture texture = null;
        if (context != null && context.assets != null) {
            texture = context.assets.getTexture(BG_PATH);
        }
        if (texture == null) {
            texture = new com.badlogic.gdx.graphics.Texture(bgFile);
        }
        texture.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        bgTex = texture;
        bgImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex))
        );
        bgImage.setFillParent(false);
        bgImage.setColor(1f, 1f, 1f, 1f);
        setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void hide() {
        super.hide();
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        // bgTex may be owned by AssetManager; don't dispose here.
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    private Table makeCard(Skin skin, UiFactory ui, String name, int hp, int atk, String ability) {
        Table card = new Table();
        card.setBackground(skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        card.defaults().pad(6);

        card.setClip(true);

        // Avoid scaling: it breaks layout sizing in ScrollPane,
        // causing the whole list to collapse into a single block.
        card.setTransform(false);

        Image icon = Scene2dPlaceholders.coloredSquare(skin, new Color(0.3f, 0.45f, 0.7f, 1f));
        Table left = new Table();
        left.add(icon).size(56);

        Table right = new Table();
        right.defaults().left();
        right.add(ui.label(name)).row();
        right.add(ui.label("HP: " + hp + "   ATK: " + atk)).row();
        right.add(ui.label(ability));

        card.add(left).padRight(8);
        card.add(right).expandX().fillX();
        return card;
    }
}
