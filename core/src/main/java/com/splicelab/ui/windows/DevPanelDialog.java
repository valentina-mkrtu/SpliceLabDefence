package com.splicelab.ui.windows;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.splicelab.app.GameContext;
import com.splicelab.model.CurrencyType;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.ui.UiFactory;

public final class DevPanelDialog extends Dialog {
    private static final int MAX_LEVEL = 50;

    private final GameContext context;
    private final Runnable onStateChanged;

    public DevPanelDialog(Skin skin, GameContext context) {
        this(skin, context, null, null, null);
    }

    public DevPanelDialog(
            Skin skin,
            GameContext context,
            Runnable onStateChanged,
            Runnable onWinNow,
            Runnable onLoseNow
    ) {
        super("Dev Panel", skin);
        this.context = context;
        this.onStateChanged = onStateChanged;

        setModal(true);
        setMovable(false);
        setResizable(false);
        pad(18);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table content = new Table();
        content.defaults().pad(8).expandX().fillX();

        content.add(makeButton(ui, "+1000 DNA", () -> context.economy.add(CurrencyType.DNA, 1000))).row();
        content.add(makeButton(ui, "+100 CRY", () -> context.economy.add(CurrencyType.CRY, 100))).row();
        content.add(makeButton(ui, "+5 of every boost", () -> {
            for (ShopDialog.PurchaseType t : ShopDialog.PurchaseType.values()) {
                context.boosts.grant(t.name(), 5);
            }
        })).row();

        content.add(makeButton(ui, "Unlock next level", this::unlockNextLevel)).row();
        content.add(makeButton(ui, "Unlock ALL levels", this::unlockAllLevels)).row();
        content.add(makeButton(ui, "Unlock all fusions/entities/items", this::unlockAllCollectibles)).row();

        if (onWinNow != null) {
            content.add(makeButton(ui, "Win level now", onWinNow)).row();
        }
        if (onLoseNow != null) {
            content.add(makeButton(ui, "Lose level now", onLoseNow)).row();
        }

        content.add(makeButton(ui, "Reset save", () -> context.saves.reset())).row();
        content.add(makeButton(ui, "Close", this::hide)).row();

        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        if (scroll.getStyle() != null) {
            scroll.getStyle().background = null;
            scroll.getStyle().hScroll = null;
            scroll.getStyle().hScrollKnob = null;
            scroll.getStyle().vScroll = null;
            scroll.getStyle().vScrollKnob = null;
        }

        getContentTable().add(scroll).width(420).height(620).pad(18);
        getButtonTable().clearChildren();
        pack();
    }

    private TextButton makeButton(UiFactory ui, String text, Runnable action) {
        TextButton b = ui.textButton(text);
        b.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (action != null) action.run();
                if (onStateChanged != null) onStateChanged.run();
            }
        });
        return b;
    }

    private void unlockNextLevel() {
        int current = context.saves.get().currentLevel;
        int next = Math.min(MAX_LEVEL, Math.max(1, current + 1));
        context.saves.get().completedLevels.add(Math.max(1, next - 1));
        context.saves.get().currentLevel = next;
        context.saves.save();
    }

    private void unlockAllLevels() {
        for (int i = 1; i <= MAX_LEVEL; i++) {
            context.saves.get().completedLevels.add(i);
        }
        context.saves.get().currentLevel = MAX_LEVEL;
        context.saves.save();
    }

    private void unlockAllCollectibles() {
        for (EntityType e : EntityType.values()) context.unlocks.unlockEntity(e);
        for (ItemType i : ItemType.values()) context.unlocks.unlockItem(i);
        context.definitions.allFusions().forEach(f -> context.fusionUnlocks.unlock(f.entityType.name() + "+" + f.itemType.name()));
    }
}
