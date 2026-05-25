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
        view.setLabListener(() -> game.setScreen(new LabGameScreen(game, context, 1)));
        view.setMapListener(() -> showSingletonDialog(
                new LevelMapDialog(view.getSkin(), context, lvl -> game.setScreen(new LabGameScreen(game, context, lvl))),
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
        dialog.show(stage);

        // Size dialogs relative to the current viewport so they don't appear huge on desktop.
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        float dw = Math.min(dialog.getWidth(), w * 0.92f);
        float dh = Math.min(dialog.getHeight(), h * 0.82f);
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
        showSingletonDialog(shopDialog, DialogType.SHOP);
    }

    private void showSettings() {
        if (settingsDialog == null) settingsDialog = new SettingsDialog(view.getSkin(), context);
        if (settingsDialog instanceof SettingsDialog s) s.showBackground(stage);
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
        if (dnaCost <= 0) return;
        if (context.economy.spend(CurrencyType.DNA, dnaCost)) {
            if (type != null) {
                context.saves.get().ownedShopPurchases.add(type.name());
                context.saves.save();
            }
            view.refresh(context);
        }
    }

    @Override
    protected void update(float delta) {
        if (view != null) view.refresh(context);
    }
}
