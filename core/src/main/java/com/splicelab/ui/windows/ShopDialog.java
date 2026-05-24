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
    public interface PurchaseListener {
        void onPurchase(int dnaCost);
    }

    public ShopDialog(Skin skin, GameContext context, PurchaseListener listener) {
        super("Shop", skin);

        UiFactory ui = new UiFactory(skin, context.audio);
        Table content = getContentTable();
        content.defaults().pad(10).expandX().fillX();

        content.add(ui.label("Spend DNA on boosts")).row();

        content.add(makeItemRow(skin, ui, context, listener, "Time Freeze", 50)).row();
        content.add(makeItemRow(skin, ui, context, listener, "Immediate Cooldown", 40)).row();
        content.add(makeItemRow(skin, ui, context, listener, "ATK x2", 80)).row();
        content.add(makeItemRow(skin, ui, context, listener, "Tube HP Recovery", 30)).row();

        button("Close");

        setModal(true);
        setMovable(false);
        pad(12);
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
