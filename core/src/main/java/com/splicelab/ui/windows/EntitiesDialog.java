package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.splicelab.app.GameContext;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.ingredient.EntityDefinition;
import com.splicelab.model.ingredient.ItemDefinition;
import com.splicelab.ui.IngredientArt;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class EntitiesDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private static final float TEXT_SCALE = 0.70f;

    private final GameContext context;

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseImageFactory.CloseImage closeButton;

    public EntitiesDialog(Skin skin, GameContext context) {
        super("Entities", skin);
        this.context = context;

        // Hide the window title text (keep layout/padding stable).
        getTitleLabel().setText("");

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
        closeButton = DialogCloseImageFactory.create(context.assets);
        Image closeBtn = closeButton.image;
        closeBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        topRight.top().right();
        topRight.add(closeBtn).size(48).padTop(50).padRight(58);
        addActor(topRight);

        Table list = new Table();
        // Pull cards inward so they never touch the frame.
        list.defaults().pad(10).expandX().fillX();
        // Force a fixed card width (prevents full-bleed rows).
        list.defaults().width(330f);

        list.add(sectionHeader(ui, "Entities")).left().row();
        for (EntityType type : EntityType.values()) {
            EntityDefinition def = context.definitions.getEntity(type).orElse(null);
            if (def == null) continue;
            boolean unlocked = context.unlocks.isEntityUnlocked(type);
            list.add(makeEntityCard(skin, ui, type, def, unlocked)).row();
        }

        list.add(sectionHeader(ui, "Items")).left().padTop(12).row();
        for (ItemType type : ItemType.values()) {
            ItemDefinition def = context.definitions.getItem(type).orElse(null);
            if (def == null) continue;
            boolean unlocked = context.unlocks.isItemUnlocked(type);
            list.add(makeItemCard(skin, ui, type, def, unlocked)).row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
        if (scroll.getStyle() != null) scroll.getStyle().background = null;

        // Match Account window sizing so all windows align.
        // Slightly more vertical padding so first/last card never spill.
        getContentTable().add(scroll).width(420).height(470).pad(30);
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
        float h = vh * 0.76f * 0.80f;
        setSize(w, h);
    }

    public void syncBackground() {
        if (bgImage == null) return;
        bgImage.setSize(getWidth(), getHeight());
        // Follow the dialog position (MainLobbyScreen centers dialogs).
        bgImage.setPosition(getX(), getY());
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded(this.context);
        if (bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        // Keep the background visible (above the lobby root) and directly behind this dialog.
        toFront();
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
            bgImage = com.splicelab.ui.Scene2dPlaceholders.coloredSquare(
                    getSkin(),
                    new com.badlogic.gdx.graphics.Color(0.12f, 0.13f, 0.17f, 1f)
            );
            bgImage.setFillParent(false);
            bgImage.setColor(1f, 1f, 1f, 1f);
            return;
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
        // Hide instantly (no fade) so the window background doesn't "blink".
        super.hide(null);
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        // bgTex may be owned by AssetManager; don't dispose here.
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    private Table sectionHeader(UiFactory ui, String title) {
        var l = ui.label(title == null ? "" : title);
        l.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        l.setFontScale(1.2f * TEXT_SCALE);
        Table t = new Table();
        t.add(l).left();
        return t;
    }

    private Table makeEntityCard(Skin skin, UiFactory ui, EntityType type, EntityDefinition def, boolean unlocked) {
        Table card = makeBaseCard(skin);

        Drawable d = context.assets.getDrawable(IngredientArt.entityIconPath(type));
        Image icon = d != null ? new Image(d) : Scene2dPlaceholders.coloredSquare(skin, new Color(0.3f, 0.45f, 0.7f, 1f));
        if (!unlocked) icon.setColor(0.55f, 0.55f, 0.55f, 1f);

        Table left = new Table();
        left.add(icon).size(56);

        String name = def.displayName;
        if (name == null || name.isBlank()) name = type.name();

        Table right = new Table();
        right.defaults().left();
        var nameLabel = ui.label(name + (unlocked ? "" : " (Locked)"));
        nameLabel.setFontScale(TEXT_SCALE);
        right.add(nameLabel).row();

        var statsLabel = ui.smallLabel("HP: " + def.baseStats.maxHp() + "   ATK: " + def.baseStats.atk());
        statsLabel.setFontScale(TEXT_SCALE);
        right.add(statsLabel).row();

        if (def.description != null && !def.description.isBlank()) {
            var desc = ui.smallLabel(def.description);
            desc.setFontScale(TEXT_SCALE);
            right.add(desc).row();
        }

        card.add(left).padRight(8);
        card.add(right).expandX().fillX();
        return card;
    }

    private Table makeItemCard(Skin skin, UiFactory ui, ItemType type, ItemDefinition def, boolean unlocked) {
        Table card = makeBaseCard(skin);

        Drawable d = context.assets.getDrawable(IngredientArt.itemIconPath(type));
        Image icon = d != null ? new Image(d) : Scene2dPlaceholders.coloredSquare(skin, new Color(0.3f, 0.45f, 0.7f, 1f));
        if (!unlocked) icon.setColor(0.55f, 0.55f, 0.55f, 1f);

        Table left = new Table();
        left.add(icon).size(56);

        String name = def.displayName;
        if (name == null || name.isBlank()) name = type.name();

        Table right = new Table();
        right.defaults().left();
        var nameLabel = ui.label(name + (unlocked ? "" : " (Locked)"));
        nameLabel.setFontScale(TEXT_SCALE);
        right.add(nameLabel).row();

        String effect = def.description;
        if (effect == null || effect.isBlank()) {
            effect = "HP +" + def.statModifiers.hp() + ", ATK +" + def.statModifiers.atk();
        }
        var effectLabel = ui.smallLabel(effect);
        effectLabel.setFontScale(TEXT_SCALE);
        right.add(effectLabel).row();

        card.add(left).padRight(8);
        card.add(right).expandX().fillX();
        return card;
    }

    private static Table makeBaseCard(Skin skin) {
        Table card = new Table();
        card.setBackground(skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        card.defaults().pad(6);
        card.setClip(true);
        card.setTransform(false);
        return card;
    }
}
