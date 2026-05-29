package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.splicelab.app.GameContext;
import com.splicelab.model.CurrencyType;
import com.splicelab.ui.UiFactory;

import java.util.ArrayList;
import java.util.List;

public final class ShopDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";
    private static final String ITEM_ROW_BG_PATH = "art/backgrounds/menuitembg.png";

    private static final float CONTENT_WIDTH = 360f;
    private static final float CONTENT_HEIGHT = 460f;
    private static final float ROW_MAX_WIDTH = 320f;
    private static final float BUY_BUTTON_WIDTH = 84f;
    private static final float BUY_BUTTON_HEIGHT = 38f;

    private final GameContext context;

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseImageFactory.CloseImage closeButton;

    private Label dnaBalanceLabel;
    private final List<RowWidgets> rows = new ArrayList<>();

    public enum PurchaseType {
        TIME_FREEZE,
        IMMEDIATE_COOLDOWN,
        ATK_X2,
        TUBE_HP_RECOVERY,
        REMOVE_ITEM
    }

    public interface PurchaseListener {
        void onPurchase(PurchaseType type, int dnaCost);
    }

    public ShopDialog(Skin skin, GameContext context, PurchaseListener listener) {
        super("Shop", skin);
        this.context = context;

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);

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
        topRight.add(closeBtn).size(48).padTop(38).padRight(58);
        addActor(topRight);

        Table content = new Table();
        content.defaults().pad(6).expandX().fillX();
        // Keep rows inside the fixed window content area.
        content.defaults().maxWidth(ROW_MAX_WIDTH);

        dnaBalanceLabel = ui.label("DNA: 0");
        content.add(dnaBalanceLabel).row();
        content.add(ui.smallLabel("Spend DNA on boosts")).row();

        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.TIME_FREEZE, "Time Freeze", 50)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.IMMEDIATE_COOLDOWN, "Immediate Cooldown", 40)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.ATK_X2, "ATK x2", 80)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.TUBE_HP_RECOVERY, "Tube HP Recovery", 30)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.REMOVE_ITEM, "Remove 1 Item", 35)).row();

        content.add(ui.smallLabel("Note: boosts work in combat")).row();

        // Keep content comfortably inside the PNG frame.
        getContentTable().add(content).width(CONTENT_WIDTH).height(CONTENT_HEIGHT).pad(22);
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

        refresh(context);
    }

    public void refresh(GameContext context) {
        if (context == null) return;
        int dna = context.economy == null ? 0 : context.economy.getBalance(CurrencyType.DNA);
        if (dnaBalanceLabel != null) dnaBalanceLabel.setText("DNA: " + dna);

        for (RowWidgets r : rows) {
            int owned = context.boosts == null ? 0 : context.boosts.count(r.type.name());
            r.ownedLabel.setText("Owned: " + owned);
            r.buyButton.setText("Buy");
        }
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
        // Follow the dialog position (MainLobbyScreen centers dialogs).
        bgImage.setPosition(getX(), getY());
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded(this.context);
        if (bgImage == null) {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "ShopDialog bgImage null");
            return;
        }
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
        super.hide();
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        // bgTex may be owned by AssetManager; don't dispose here.
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    private Table makeItemRow(Skin skin, UiFactory ui, GameContext context, PurchaseListener listener, PurchaseType type, String name, int cost) {
        Table row = new Table();
        Drawable rowBg = context != null && context.assets != null ? context.assets.getDrawable(ITEM_ROW_BG_PATH) : null;
        row.setBackground(rowBg != null ? rowBg : skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        row.defaults().pad(6);

        // Avoid scaling: it breaks layout sizing in Dialog/Table.
        row.setTransform(false);

        row.add(ui.label(name)).expandX().left();
        row.add(ui.label(cost + " DNA")).right().padRight(8);
        Label ownedLabel = ui.smallLabel("Owned: 0");
        row.add(ownedLabel).right().padRight(8);

        TextButton buy = ui.textButton("Buy");
        buy.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (context.economy.canSpend(CurrencyType.DNA, cost)) {
                    if (listener != null) listener.onPurchase(type, cost);
                    refresh(context);
                } else {
                    buy.setText("No DNA");
                    buy.clearActions();
                    buy.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.75f),
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> buy.setText("Buy"))
                    ));
                }
            }
        });
        row.add(buy).width(BUY_BUTTON_WIDTH).height(BUY_BUTTON_HEIGHT);

        rows.add(new RowWidgets(type, ownedLabel, buy));
        return row;
    }

    private record RowWidgets(PurchaseType type, Label ownedLabel, TextButton buyButton) {
    }
}
