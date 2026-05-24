package com.splicelab.ui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.app.GameContext;
import com.splicelab.data.SaveData;
import com.splicelab.model.CurrencyType;
import com.splicelab.services.NameGenerator;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;

public final class MainLobbyView {
    private static final String ICON_BG_PATH = "art/icons/iconbg.png";
    private static final String ICON_ACCOUNT_PATH = "art/icons/account.png";
    private static final String ICON_COLLECTIONS_PATH = "art/icons/collections.png";
    private static final String ICON_ENTITIES_PATH = "art/icons/entities.png";
    private static final String ICON_SHOP_PATH = "art/icons/shop.png";
    private static final String ICON_PFP_PATH = "art/icons/pfp.png";
    private static final String ICON_LAB_PATH = "art/icons/thelab.png";
    private static final String BG_LAB_PATH = "art/backgrounds/thelab.png";
    private static final String BG_MAP_PATH = "art/backgrounds/themap.png";
    private static final String BG_MAIN_PATH = "art/backgrounds/mainbg.png";
    private static final String ICON_DNA_PATH = "art/icons/dna.png";
    private static final String ICON_CRY_PATH = "art/icons/cry.png";

    private final Skin skin;
    private final UiFactory ui;
    private final Table root;

    private final Label usernameLabel;
    private final Label levelLabel;
    private final Label dnaLabel;
    private final Label crystalsLabel;

    private Runnable labListener;
    private Runnable mapListener;

    private Runnable accountListener;
    private Runnable collectionsListener;
    private Runnable entitiesListener;
    private Runnable shopListener;

    private Texture iconBgTex;
    private Texture accountTex;
    private Texture collectionsTex;
    private Texture entitiesTex;
    private Texture shopTex;
    private Texture pfpTex;
    private Texture labTex;
    private Texture labBgTex;
    private Texture mapBgTex;
    private Texture mainBgTex;
    private Texture dnaTex;
    private Texture cryTex;

    public MainLobbyView(GameContext context) {
        skin = PlaceholderSkinFactory.create();
        ui = new UiFactory(skin, context.audio);

        SaveData save = context.saves.get();
        if (save.playerName == null || save.playerName.isBlank()) {
            save.playerName = new NameGenerator().generate();
            context.saves.save();
        }

        root = new Table();
        root.setFillParent(true);
        root.defaults().expand().fill();
        Drawable mainBg = tryLoadIcon(BG_MAIN_PATH, () -> mainBgTex, t -> mainBgTex = t);
        if (mainBg != null) root.setBackground(mainBg);
        else root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        Table top = new Table();
        top.pad(12);

        Table profile = new Table();
        profile.defaults().left();
        Image pfp = makeIconImage(
                ICON_PFP_PATH,
                new Color(0.25f, 0.3f, 0.45f, 1f),
                () -> pfpTex,
                t -> pfpTex = t
        );
        profile.add(pfp).size(64).row();
        usernameLabel = ui.label(save.playerName);
        profile.add(usernameLabel).padTop(6);

        Table currencies = new Table();
        currencies.defaults().right();
        Table dnaRow = new Table();
        Drawable dnaIcon = tryLoadIcon(ICON_DNA_PATH, () -> dnaTex, t -> dnaTex = t);
        if (dnaIcon != null) dnaRow.add(new Image(dnaIcon)).size(18).padRight(6);
        else dnaRow.add(Scene2dPlaceholders.iconLabel(skin, "DNA")).padRight(6);
        dnaLabel = ui.label(String.valueOf(context.economy.getBalance(CurrencyType.DNA)));
        dnaRow.add(dnaLabel);
        Table crystalRow = new Table();
        Drawable cryIcon = tryLoadIcon(ICON_CRY_PATH, () -> cryTex, t -> cryTex = t);
        if (cryIcon != null) crystalRow.add(new Image(cryIcon)).size(18).padRight(6);
        else crystalRow.add(Scene2dPlaceholders.iconLabel(skin, "CRY")).padRight(6);
        crystalsLabel = ui.label(String.valueOf(context.economy.getBalance(CurrencyType.CRYSTALS)));
        crystalRow.add(crystalsLabel);
        currencies.add(dnaRow).row();
        currencies.add(crystalRow).padTop(4);

        levelLabel = ui.label("Level " + save.playerLevel);

        top.add(profile).expandX().left();
        top.add(levelLabel).expandX().center();
        top.add(currencies).expandX().right();

        Table center = new Table();
        center.defaults().pad(12);
        center.padTop(200);
        TextButton labBtn = ui.textButton("");
        TextButton mapBtn = ui.textButton("");

        Drawable labBg = tryLoadIcon(BG_LAB_PATH, () -> labBgTex, t -> labBgTex = t);
        if (labBg != null) {
            var s = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle(labBtn.getStyle());
            s.up = labBg;
            s.down = labBg;
            labBtn.setStyle(s);
        }
        Drawable mapBg = tryLoadIcon(BG_MAP_PATH, () -> mapBgTex, t -> mapBgTex = t);
        if (mapBg != null) {
            var s = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle(mapBtn.getStyle());
            s.up = mapBg;
            s.down = mapBg;
            mapBtn.setStyle(s);
        }
        labBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (labListener != null) labListener.run();
            }
        });
        mapBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (mapListener != null) mapListener.run();
            }
        });
        center.add(labBtn).width(480).height(220).row();
        center.add(mapBtn).width(480).height(220);

        Table bottom = new Table();
        bottom.setBackground(skin.newDrawable("white", UiConstants.PANEL_DARK));
        bottom.defaults().pad(10).expandX();

        ImageButton accountBtn = makeNavButton(ICON_ACCOUNT_PATH, () -> accountTex, t -> accountTex = t);
        ImageButton collectionsBtn = makeNavButton(ICON_COLLECTIONS_PATH, () -> collectionsTex, t -> collectionsTex = t);
        ImageButton entitiesBtn = makeNavButton(ICON_ENTITIES_PATH, () -> entitiesTex, t -> entitiesTex = t);
        ImageButton shopBtn = makeNavButton(ICON_SHOP_PATH, () -> shopTex, t -> shopTex = t);

        accountBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (accountListener != null) accountListener.run();
            }
        });
        collectionsBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (collectionsListener != null) collectionsListener.run();
            }
        });
        entitiesBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (entitiesListener != null) entitiesListener.run();
            }
        });
        shopBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (shopListener != null) shopListener.run();
            }
        });

        float navSize = 64f * 1.2f;
        bottom.padBottom(18);
        bottom.add(accountBtn).size(navSize);
        bottom.add(collectionsBtn).size(navSize);
        bottom.add(entitiesBtn).size(navSize);
        bottom.add(shopBtn).size(navSize);

        root.add(top).expandX().fillX().top().row();
        root.add(center).expand().fill().row();
        root.add(bottom).expandX().fillX().bottom().height(110);
    }

    public Actor getRoot() {
        return root;
    }

    public Skin getSkin() {
        return skin;
    }

    public void refresh(GameContext context) {
        SaveData save = context.saves.get();
        usernameLabel.setText(save.playerName);
        levelLabel.setText("Level " + save.playerLevel);
        dnaLabel.setText(String.valueOf(context.economy.getBalance(CurrencyType.DNA)));
        crystalsLabel.setText(String.valueOf(context.economy.getBalance(CurrencyType.CRYSTALS)));
    }

    public void setLabListener(Runnable labListener) {
        this.labListener = labListener;
    }

    public void setMapListener(Runnable mapListener) {
        this.mapListener = mapListener;
    }

    public void setAccountListener(Runnable accountListener) {
        this.accountListener = accountListener;
    }

    public void setCollectionsListener(Runnable collectionsListener) {
        this.collectionsListener = collectionsListener;
    }

    public void setEntitiesListener(Runnable entitiesListener) {
        this.entitiesListener = entitiesListener;
    }

    public void setShopListener(Runnable shopListener) {
        this.shopListener = shopListener;
    }

    public void dispose() {
        if (iconBgTex != null) iconBgTex.dispose();
        if (accountTex != null) accountTex.dispose();
        if (collectionsTex != null) collectionsTex.dispose();
        if (entitiesTex != null) entitiesTex.dispose();
        if (shopTex != null) shopTex.dispose();
        if (pfpTex != null) pfpTex.dispose();
        if (labTex != null) labTex.dispose();
        if (labBgTex != null) labBgTex.dispose();
        if (mapBgTex != null) mapBgTex.dispose();
        if (mainBgTex != null) mainBgTex.dispose();
        if (dnaTex != null) dnaTex.dispose();
        if (cryTex != null) cryTex.dispose();
        skin.dispose();
    }

    private Image makeIconImage(String path, Color fallbackColor, TexGetter getter, TexSetter setter) {
        Drawable d = tryLoadIcon(path, getter, setter);
        if (d != null) return new Image(d);
        return Scene2dPlaceholders.coloredSquare(skin, fallbackColor);
    }

    private interface TexGetter {
        Texture get();
    }

    private interface TexSetter {
        void set(Texture texture);
    }

    private Drawable tryLoadIcon(String path, TexGetter getter, TexSetter setter) {
        if (path == null) return null;
        if (!com.badlogic.gdx.Gdx.files.internal(path).exists()) return null;
        Texture existing = getter.get();
        if (existing == null) {
            existing = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
            existing.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            setter.set(existing);
        }
        return new TextureRegionDrawable(new TextureRegion(existing));
    }

    private ImageButton makeNavButton(String iconPath, TexGetter iconGetter, TexSetter iconSetter) {
        Drawable bg = tryLoadIcon(ICON_BG_PATH, () -> iconBgTex, t -> iconBgTex = t);
        Drawable icon = tryLoadIcon(iconPath, iconGetter, iconSetter);

        if (bg == null || icon == null) {
            return new ImageButton(skin.newDrawable("white", UiConstants.PANEL_DARK));
        }

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = bg;
        style.down = bg;
        style.imageUp = icon;
        style.imageDown = icon;
        return new ImageButton(style);
    }
}
