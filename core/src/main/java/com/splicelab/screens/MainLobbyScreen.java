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

        view.setAccountListener(() -> showSingletonDialog(new AccountDialog(view.getSkin(), context), DialogType.ACCOUNT));
        view.setCollectionsListener(() -> showSingletonDialog(new CollectionsDialog(view.getSkin(), context), DialogType.COLLECTIONS));
        view.setEntitiesListener(() -> showSingletonDialog(new EntitiesDialog(view.getSkin(), context), DialogType.ENTITIES));
        view.setShopListener(() -> showSingletonDialog(new ShopDialog(view.getSkin(), context, this::onShopPurchase), DialogType.SHOP));

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
