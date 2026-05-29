package com.splicelab.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.model.CurrencyType;
import com.splicelab.ui.screens.MainLobbyView;
import com.splicelab.ui.windows.AccountDialog;
import com.splicelab.ui.windows.CollectionsDialog;
import com.splicelab.ui.windows.EntitiesDialog;
import com.splicelab.ui.windows.LevelMapDialog;
import com.splicelab.ui.windows.SettingsDialog;
import com.splicelab.ui.windows.ShopDialog;
import com.splicelab.ui.windows.DevPanelDialog;
import com.splicelab.debug.DebugFlags;

public final class MainLobbyScreen extends BaseScreen {
    private MainLobbyView view;

    private Dialog accountDialog;
    private Dialog collectionsDialog;
    private Dialog entitiesDialog;
    private Dialog shopDialog;
    private Dialog mapDialog;
    private Dialog settingsDialog;

    public MainLobbyScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    protected void onResumeScreen() {
        context.audio.startMainpageLoop();
    }

    @Override
    protected void onPauseScreen() {
        context.audio.stopMainpageLoop();
    }

    @Override
    public void dispose() {
        context.audio.stopMainpageLoop();
        super.dispose();
        if (view != null) view.dispose();
    }

    @Override
    protected void buildUi() {
        view = new MainLobbyView(context);
        view.setLabListener(() -> game.setScreen(new LabGameScreen(game, context, context.saves.get().currentLevel)));
        view.setMapListener(() -> showSingletonDialog(
                new LevelMapDialog(view.getSkin(), context, lvl -> {
                    // Keep lobby "Level X" label in sync with the selected map level.
                    context.saves.get().currentLevel = lvl;
                    context.saves.save();
                    game.setScreen(new LabGameScreen(game, context, lvl));
                }),
                DialogType.MAP
        ));

        view.setAccountListener(this::showAccount);
        view.setCollectionsListener(this::showCollections);
        view.setEntitiesListener(this::showEntities);
        view.setShopListener(this::showShop);
        view.setSettingsListener(this::showSettings);

        stage.addActor(view.getRoot());
    }

    private enum DialogType {
        ACCOUNT,
        COLLECTIONS,
        ENTITIES,
        SHOP,
        MAP,
        SETTINGS
    }

    private void showSingletonDialog(Dialog dialog, DialogType type) {
        if (dialog == null) return;

        hideAllDialogs();

        switch (type) {
            case ACCOUNT -> accountDialog = dialog;
            case COLLECTIONS -> collectionsDialog = dialog;
            case ENTITIES -> entitiesDialog = dialog;
            case SHOP -> shopDialog = dialog;
            case MAP -> mapDialog = dialog;
            case SETTINGS -> settingsDialog = dialog;
        }

        // Avoid Dialog's default fade-in animation; we want instant UI response.
        dialog.show(stage, null);

        // Size dialogs relative to the current viewport so they don't appear huge on desktop.
        // Use pref size (pack()) so layout is stable, then clamp to viewport.
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        float dw;
        float dh;

        // Keep the main menu windows perfectly consistent in size/position so
        // switching between them doesn't cause visible snapping.
        boolean fixedMenuWindow = dialog instanceof AccountDialog
                || dialog instanceof CollectionsDialog
                || dialog instanceof EntitiesDialog
                || dialog instanceof ShopDialog;

        boolean fixedMapWindow = dialog instanceof LevelMapDialog;

        if (fixedMenuWindow) {
            dw = w * 0.90f;
            // 20% shorter than the previous 0.76f height.
            dh = h * 0.76f * 0.80f;
        } else if (fixedMapWindow) {
            // Make the map window taller so the background frame feels less cramped.
            dw = w * 0.90f;
            // 20% shorter than the previous map size.
            dh = Math.min(h * 0.92f, h * 0.76f * 1.20f * 0.80f);
        } else {
            dw = Math.min(dialog.getPrefWidth(), w * 0.92f);
            dh = Math.min(dialog.getPrefHeight(), h * 0.82f);
        }
        dialog.setSize(dw, dh);
        dialog.setPosition((w - dw) * 0.5f, (h - dh) * 0.5f);

        // Ensure dialog background is visible under the dialog (after final size/position).
        if (dialog instanceof AccountDialog d) {
            d.showBackground(stage);
            d.syncBackground();
        }
        if (dialog instanceof CollectionsDialog d) {
            d.showBackground(stage);
            d.syncBackground();
        }
        if (dialog instanceof EntitiesDialog d) {
            d.showBackground(stage);
            d.syncBackground();
        }
        if (dialog instanceof ShopDialog d) {
            d.showBackground(stage);
            d.syncBackground();
        }
        if (dialog instanceof LevelMapDialog d) {
            d.showBackground(stage);
            d.syncBackground();
        }
    }

    private void showAccount() {
        if (accountDialog == null) accountDialog = new AccountDialog(view.getSkin(), context);
        showSingletonDialog(accountDialog, DialogType.ACCOUNT);
    }

    private void showCollections() {
        if (collectionsDialog == null) collectionsDialog = new CollectionsDialog(view.getSkin(), context);
        showSingletonDialog(collectionsDialog, DialogType.COLLECTIONS);
    }

    private void showEntities() {
        if (entitiesDialog == null) entitiesDialog = new EntitiesDialog(view.getSkin(), context);
        showSingletonDialog(entitiesDialog, DialogType.ENTITIES);
    }

    private void showShop() {
        if (shopDialog == null) shopDialog = new ShopDialog(view.getSkin(), context, this::onShopPurchase);
        if (shopDialog instanceof ShopDialog d) d.refresh(context);
        showSingletonDialog(shopDialog, DialogType.SHOP);
    }

    private void showSettings() {
        if (settingsDialog == null) settingsDialog = new SettingsDialog(view.getSkin(), context);
        showSingletonDialog(settingsDialog, DialogType.SETTINGS);
    }

    private void hideAllDialogs() {
        if (accountDialog != null) accountDialog.hide();
        if (collectionsDialog != null) collectionsDialog.hide();
        if (entitiesDialog != null) entitiesDialog.hide();
        if (shopDialog != null) shopDialog.hide();
        if (mapDialog != null) mapDialog.hide();
        if (settingsDialog != null) settingsDialog.hide();
    }

    private void onShopPurchase(ShopDialog.PurchaseType type, int dnaCost) {
        if (type == null || dnaCost <= 0) return;
        if (context.economy.spend(CurrencyType.DNA, dnaCost)) {
            context.boosts.grant(type.name(), 1);
            view.refresh(context);
            if (shopDialog instanceof ShopDialog d) d.refresh(context);
        }
    }

    @Override
    protected void openDevPanel() {
        if (!DebugFlags.DEBUG) return;
        new DevPanelDialog(
                view.getSkin(),
                context,
                () -> {
                    if (view != null) view.refresh(context);
                    if (shopDialog instanceof ShopDialog d) d.refresh(context);
                },
                null,
                null
        ).show(stage);
    }

    @Override
    protected void update(float delta) {
        // Avoid per-frame UI mutations (label text/layout invalidation) which can make
        // the scene feel heavy on desktop. Refresh only when the data changes.
        if (view != null) view.refreshIfNeeded(context);
    }
}
