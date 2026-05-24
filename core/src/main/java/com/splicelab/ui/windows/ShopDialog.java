package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.model.CurrencyType;
import com.splicelab.ui.UiFactory;

public final class ShopDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/menuwindowbg.png";

    private com.badlogic.gdx.graphics.Texture bgTex;

    public interface PurchaseListener {
        void onPurchase(int dnaCost);
    }

    public ShopDialog(Skin skin, GameContext context, PurchaseListener listener) {
        super("Shop", skin);

        if (com.badlogic.gdx.Gdx.files.internal(BG_PATH).exists()) {
            bgTex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(BG_PATH));
            bgTex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            var bg = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(bgTex));
            getContentTable().setBackground(bg);
            getButtonTable().setBackground(bg);
        }

        UiFactory ui = new UiFactory(skin, context.audio);
        Table content = new Table();
        content.defaults().pad(10).expandX().fillX();

        content.add(ui.label("Spend DNA on boosts")).row();

        content.add(makeItemRow(skin, ui, context, listener, "Time Freeze", 50)).row();
        content.add(makeItemRow(skin, ui, context, listener, "Immediate Cooldown", 40)).row();
        content.add(makeItemRow(skin, ui, context, listener, "ATK x2", 80)).row();
        content.add(makeItemRow(skin, ui, context, listener, "Tube HP Recovery", 30)).row();

        getContentTable().add(content).width(440).height(520).pad(10);
        var close = ui.textButton("Close");
        close.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        getButtonTable().add(close);

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(12);

        setSize(480, 650);
    }

    @Override
    public void hide() {
        super.hide();
        if (bgTex != null) bgTex.dispose();
        bgTex = null;
    }

    private Table makeItemRow(Skin skin, UiFactory ui, GameContext context, PurchaseListener listener, String name, int cost) {
        Table row = new Table();
        row.setBackground(skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        row.defaults().pad(8);

        row.add(ui.label(name)).expandX().left();
        row.add(ui.label(cost + " DNA")).right().padRight(8);

        TextButton buy = ui.textButton("Buy");
        buy.addListener(e -> {
            if (context.economy.canSpend(CurrencyType.DNA, cost)) {
                if (listener != null) listener.onPurchase(cost);
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
