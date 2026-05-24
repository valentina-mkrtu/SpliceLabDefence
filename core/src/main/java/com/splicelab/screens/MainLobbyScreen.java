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
import com.splicelab.ui.windows.ShopDialog;

public final class MainLobbyScreen extends BaseScreen {
    private MainLobbyView view;

    private Dialog accountDialog;
    private Dialog collectionsDialog;
    private Dialog entitiesDialog;
    private Dialog shopDialog;
    private Dialog mapDialog;

    public MainLobbyScreen(SpliceLabGame game, GameContext context) {
        super(game, context);
    }

    @Override
    public void dispose() {
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

        stage.addActor(view.getRoot());
    }

    private enum DialogType {
        ACCOUNT,
        COLLECTIONS,
        ENTITIES,
        SHOP,
        MAP
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
        }
        dialog.show(stage);
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

    private void hideAllDialogs() {
        if (accountDialog != null) accountDialog.hide();
        if (collectionsDialog != null) collectionsDialog.hide();
        if (entitiesDialog != null) entitiesDialog.hide();
        if (shopDialog != null) shopDialog.hide();
        if (mapDialog != null) mapDialog.hide();
    }

    private void onShopPurchase(int dnaCost) {
        if (dnaCost <= 0) return;
        if (context.economy.spend(CurrencyType.DNA, dnaCost)) {
            view.refresh(context);
        }
    }

    @Override
    protected void update(float delta) {
        if (view != null) view.refresh(context);
    }
}
