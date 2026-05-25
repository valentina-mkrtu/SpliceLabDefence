package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.splicelab.app.GameContext;
import com.splicelab.model.CurrencyType;
import com.splicelab.ui.UiFactory;

public final class ShopDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private com.badlogic.gdx.graphics.Texture bgTex;
    private com.badlogic.gdx.scenes.scene2d.ui.Image bgImage;
    private DialogCloseButtonFactory.CloseButton closeButton;

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

        // Nuke any skin-provided window/content/button backgrounds (can tint whole dialog).
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);

        // Use our PNG as the window background.
        if (getStyle() != null) getStyle().background = null;

        com.badlogic.gdx.files.FileHandle bgFile = com.badlogic.gdx.Gdx.files.internal(BG_PATH);
        if (bgFile.exists()) {
            bgTex = new com.badlogic.gdx.graphics.Texture(bgFile);
            bgTex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            bgImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex)));
            // Put the image behind content only (not full stage), sized to the dialog.
            bgImage.setFillParent(false);
            bgImage.setColor(1f, 1f, 1f, 1f);
            addActor(bgImage);
            bgImage.toBack();
            // Disable dialog tinting.
            setColor(1f, 1f, 1f, 1f);
        } else {
            com.badlogic.gdx.Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
        }

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
        topRight.add(closeBtn).size(56).pad(8);
        addActor(topRight);

        Table content = new Table();
        content.defaults().pad(10).expandX().fillX();

        content.add(ui.label("Spend DNA on boosts")).row();

        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.TIME_FREEZE, "Time Freeze", 50)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.IMMEDIATE_COOLDOWN, "Immediate Cooldown", 40)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.ATK_X2, "ATK x2", 80)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.TUBE_HP_RECOVERY, "Tube HP Recovery", 30)).row();
        content.add(makeItemRow(skin, ui, context, listener, PurchaseType.REMOVE_ITEM, "Remove 1 Item", 35)).row();

        content.add(ui.smallLabel("Note: boosts work in combat")).row();

        // Leave margin so background frame is visible.
        getContentTable().add(content).width(420).height(500).pad(22);
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
        bgImage.setPosition(0f, 0f);
    }

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null || bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        bgImage.toBack();
        bgImage.setColor(1f, 0f, 0f, 0.35f);
        syncBackground();
    }

    @Override
    public void hide() {
        super.hide();
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        if (bgTex != null) bgTex.dispose();
        bgTex = null;
        if (closeButton != null) closeButton.dispose();
        closeButton = null;
    }

    private Table makeItemRow(Skin skin, UiFactory ui, GameContext context, PurchaseListener listener, PurchaseType type, String name, int cost) {
        Table row = new Table();
        row.setBackground(skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        row.defaults().pad(8);

        row.add(ui.label(name)).expandX().left();
        row.add(ui.label(cost + " DNA")).right().padRight(8);

        TextButton buy = ui.textButton("Buy");
        buy.addListener(e -> {
            if (context.economy.canSpend(CurrencyType.DNA, cost)) {
                if (listener != null) listener.onPurchase(type, cost);
                buy.setText("Bought");
            } else {
                buy.setText("No DNA");
            }
            return true;
        });
        row.add(buy).width(120).height(44);
        return row;
    }
}
