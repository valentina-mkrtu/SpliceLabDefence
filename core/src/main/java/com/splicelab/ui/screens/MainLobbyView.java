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
import com.splicelab.app.GameContext;
import com.splicelab.data.SaveData;
import com.splicelab.model.CurrencyType;
import com.splicelab.services.NameGenerator;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.widgets.CurrencyPillWidget;

public final class MainLobbyView {
    private static final String ICON_BG_PATH = "art/icons/iconbg.png";
    private static final String ICON_ACCOUNT_PATH = "art/icons/account.png";
    private static final String ICON_COLLECTIONS_PATH = "art/icons/collections.png";
    private static final String ICON_ENTITIES_PATH = "art/icons/entities.png";
    private static final String ICON_SHOP_PATH = "art/icons/shop.png";
    private static final String ICON_PFP_PATH = "art/icons/pfp.png";
    private static final String ICON_LAB_PATH = "art/icons/lab.png";
    private static final String ICON_SETTINGS_PATH = "art/icons/settings.png";
    private static final String BG_LAB_PATH = "art/backgrounds/thelab.png";
    private static final String BG_MAP_PATH = "art/backgrounds/themap.png";
    private static final String BG_MAIN_PATH = "art/backgrounds/mainbg.png";
    private static final String BG_MENU_PATH = "art/backgrounds/menubg.png";
    private static final String BG_CURR_PATH = "art/backgrounds/currbg.png";
    private static final String ICON_DNA_PATH = "art/icons/dna.png";
    private static final String ICON_CRY_PATH = "art/icons/cry.png";

    private final Skin skin;
    private final UiFactory ui;
    private final Table root;

    private final GameContext context;

    private final Label usernameLabel;
    private final Label levelLabel;
    // Amount labels live inside CurrencyPillWidget.

    private CurrencyPillWidget dnaPill;
    private CurrencyPillWidget cryPill;

    private Runnable labListener;
    private Runnable mapListener;

    private Runnable accountListener;
    private Runnable collectionsListener;
    private Runnable entitiesListener;
    private Runnable shopListener;
    private Runnable settingsListener;

    private Texture iconBgTex;
    private Texture accountTex;
    private Texture collectionsTex;
    private Texture entitiesTex;
    private Texture shopTex;
    private Texture pfpTex;
    private Texture labTex;
    private Texture settingsTex;
    private Texture labBgTex;
    private Texture mapBgTex;
    private Texture mainBgTex;
    private Texture menuBgTex;
    private Texture currBgTex;
    private Texture dnaTex;
    private Texture cryTex;

    private String lastPlayerName;
    private int lastShownLevel = Integer.MIN_VALUE;
    private int lastDna = Integer.MIN_VALUE;
    private int lastCry = Integer.MIN_VALUE;

    public MainLobbyView(GameContext context) {
        this.context = context;
        skin = context.skin;
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

        // -----------------------------------------------------------------
        // Top header (single container so everything stays aligned)
        // -----------------------------------------------------------------
        Table header = new Table();
        header.top().left();
        header.pad(21, 20, 0, 20);

        Image pfp = makeIconImage(
                ICON_PFP_PATH,
                new Color(0.25f, 0.3f, 0.45f, 1f),
                () -> pfpTex,
                t -> pfpTex = t
        );

        usernameLabel = ui.label(save.playerName);
        usernameLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        usernameLabel.setFontScale(0.8f);

        levelLabel = ui.label("Level " + save.currentLevel);
        levelLabel.setFontScale(1.125f * 0.8f);

        Table left = new Table();
        left.add(pfp).size(58).left();
        left.add(levelLabel).padLeft(16).center().left();
        left.row();
        // Keep username centered under the avatar only.
        left.add(usernameLabel).width(58).center().padTop(6);
        left.add();

        Drawable currBg = tryLoadIcon(BG_CURR_PATH, () -> currBgTex, t -> currBgTex = t);
        Drawable dnaIcon = tryLoadIcon(ICON_DNA_PATH, () -> dnaTex, t -> dnaTex = t);
        Drawable cryIcon = tryLoadIcon(ICON_CRY_PATH, () -> cryTex, t -> cryTex = t);

        dnaPill = new CurrencyPillWidget(
                skin,
                ui,
                currBg,
                dnaIcon,
                null,
                context.economy.getBalance(CurrencyType.DNA)
        );
        cryPill = new CurrencyPillWidget(
                skin,
                ui,
                currBg,
                cryIcon,
                null,
                context.economy.getBalance(CurrencyType.CRY)
        );

        Table currencyRow = new Table();
        // Keep pills grouped together with a fixed gap, but allow the whole group
        // to scale down if the viewport is too narrow.
        float basePillW = 170f;
        float basePillH = 64f;
        float baseGap = 14f;
        // Approximate available width in the FitViewport (540px wide UI design).
        // Subtract header padding + avatar + settings + room for the level label.
        float available = 540f - (20f * 2f) - 58f - 58f - 96f;
        float currencyGroupW = basePillW * 2f + baseGap;
        float currencyScale = Math.min(1f, available / currencyGroupW);

        currencyRow.setTransform(true);
        currencyRow.setOrigin(com.badlogic.gdx.utils.Align.center);
        currencyRow.setScale(currencyScale);
        currencyRow.add(dnaPill).size(basePillW, basePillH);
        currencyRow.add().width(baseGap);
        currencyRow.add(cryPill).size(basePillW, basePillH);

        ImageButton settingsBtn = makeNavButton(ICON_SETTINGS_PATH, () -> settingsTex, t -> settingsTex = t);
        settingsBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                if (settingsListener != null) settingsListener.run();
            }
        });

        header.add(left).left().top();
        header.add(currencyRow)
                .width(currencyGroupW * currencyScale)
                .height(basePillH * currencyScale)
                .expandX()
                .center()
                .center()
                .padLeft(5);
        header.add(settingsBtn).size(58).right().top().padRight(0);

        Table center = new Table();
        center.defaults().pad(12);
        center.padTop(130);

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

        // Make press feel physical: slight scale down while pressed.
        labBtn.setTransform(true);
        mapBtn.setTransform(true);

        // Press feedback: scale up briefly, then always snap back.
        labBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                labBtn.clearActions();
                labBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                        1.05f,
                        1.05f,
                        0.04f,
                        com.badlogic.gdx.math.Interpolation.sineOut
                ));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                labBtn.clearActions();
                labBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                        1f,
                        1f,
                        0.08f,
                        com.badlogic.gdx.math.Interpolation.sineOut
                ));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                // If finger/mouse leaves while pressed, ensure scale resets.
                labBtn.clearActions();
                labBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.08f, com.badlogic.gdx.math.Interpolation.sineOut));
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (labListener != null) labListener.run();
                // Some click paths skip touchUp (stage switch/dispose). Force reset.
                labBtn.setScale(1f);
            }
        });
        mapBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                mapBtn.clearActions();
                mapBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                        1.05f,
                        1.05f,
                        0.04f,
                        com.badlogic.gdx.math.Interpolation.sineOut
                ));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                mapBtn.clearActions();
                mapBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                        1f,
                        1f,
                        0.08f,
                        com.badlogic.gdx.math.Interpolation.sineOut
                ));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                mapBtn.clearActions();
                mapBtn.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.08f, com.badlogic.gdx.math.Interpolation.sineOut));
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (mapListener != null) mapListener.run();
                mapBtn.setScale(1f);
            }
        });
        float btnW = 450f;
        float btnH = 210f;
        center.add(labBtn).width(btnW).height(btnH).row();
        center.add(mapBtn).width(btnW).height(btnH);

        Table bottom = new Table();
        Drawable menuBg = tryLoadIcon(BG_MENU_PATH, () -> menuBgTex, t -> menuBgTex = t);
        if (menuBg != null) bottom.setBackground(menuBg);
        else bottom.setBackground(skin.newDrawable("white", UiConstants.PANEL_DARK));
        bottom.defaults().pad(10).expandX();

        ImageButton accountBtn = makeNavButton(ICON_ACCOUNT_PATH, () -> accountTex, t -> accountTex = t);
        ImageButton collectionsBtn = makeNavButton(ICON_COLLECTIONS_PATH, () -> collectionsTex, t -> collectionsTex = t);
        ImageButton entitiesBtn = makeNavButton(ICON_ENTITIES_PATH, () -> entitiesTex, t -> entitiesTex = t);
        ImageButton shopBtn = makeNavButton(ICON_SHOP_PATH, () -> shopTex, t -> shopTex = t);

        accountBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                if (accountListener != null) accountListener.run();
            }
        });
        collectionsBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                if (collectionsListener != null) collectionsListener.run();
            }
        });
        entitiesBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                if (entitiesListener != null) entitiesListener.run();
            }
        });
        shopBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                if (shopListener != null) shopListener.run();
            }
        });

        float navSize = 64f * 1.2f * 1.1f;
        bottom.padBottom(8);
        bottom.add(accountBtn).size(navSize);
        bottom.add(collectionsBtn).size(navSize);
        bottom.add(entitiesBtn).size(navSize);
        bottom.add(shopBtn).size(navSize);

        root.add(header).expand(true, false).fill(true, false).top().height(120).row();
        root.add(center).expand().fill().row();
        root.add(bottom).expandX().fillX().bottom().height(130);
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
        levelLabel.setText("Level " + save.currentLevel);
        if (dnaPill != null) dnaPill.setAmount(context.economy.getBalance(CurrencyType.DNA));
        if (cryPill != null) cryPill.setAmount(context.economy.getBalance(CurrencyType.CRY));
    }

    public void refreshIfNeeded(GameContext context) {
        SaveData save = context.saves.get();
        String playerName = save.playerName;
        int currentLevel = save.currentLevel;
        int dna = context.economy.getBalance(CurrencyType.DNA);
        int cry = context.economy.getBalance(CurrencyType.CRY);

        boolean changed = false;
        if (lastPlayerName == null || !lastPlayerName.equals(playerName)) {
            lastPlayerName = playerName;
            changed = true;
        }
        if (lastShownLevel != currentLevel) {
            lastShownLevel = currentLevel;
            changed = true;
        }
        if (lastDna != dna) {
            lastDna = dna;
            changed = true;
        }
        if (lastCry != cry) {
            lastCry = cry;
            changed = true;
        }

        if (!changed) return;

        usernameLabel.setText(playerName);
        levelLabel.setText("Level " + currentLevel);
        if (dnaPill != null) dnaPill.setAmount(dna);
        if (cryPill != null) cryPill.setAmount(cry);
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

    public void setSettingsListener(Runnable settingsListener) {
        this.settingsListener = settingsListener;
    }

    public void dispose() {
        if (iconBgTex != null) iconBgTex.dispose();
        if (accountTex != null) accountTex.dispose();
        if (collectionsTex != null) collectionsTex.dispose();
        if (entitiesTex != null) entitiesTex.dispose();
        if (shopTex != null) shopTex.dispose();
        if (pfpTex != null) pfpTex.dispose();
        if (labTex != null) labTex.dispose();
        if (settingsTex != null) settingsTex.dispose();
        if (labBgTex != null) labBgTex.dispose();
        if (mapBgTex != null) mapBgTex.dispose();
        if (mainBgTex != null) mainBgTex.dispose();
        if (menuBgTex != null) menuBgTex.dispose();
        if (currBgTex != null) currBgTex.dispose();
        if (dnaTex != null) dnaTex.dispose();
        if (cryTex != null) cryTex.dispose();

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
        Texture existing = getter.get();
        if (existing == null) {
            // Prefer AssetManager (preloaded in LoadingScreen) to avoid ad-hoc loads.
            if (context != null && context.assets != null) {
                existing = context.assets.getTexture(path);
            }
            // If it's not in AssetManager, treat it as missing so we don't do blocking
            // disk IO and ad-hoc Texture allocations during UI creation.
            if (existing == null) return null;
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
