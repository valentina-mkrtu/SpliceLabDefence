package com.splicelab.ui.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;

public final class SettingsDialog extends Dialog {
    private static final String BG_PATH = "art/backgrounds/settingsbg.png";
    private static final String SOUND_ICON_PATH = "art/icons/sound.png";

    private Texture bgTex;
    private Image bgImage;
    private Texture soundIconTex;

    private Texture sliderTrackTex;
    private Texture sliderKnobTex;

    public void showBackground(com.badlogic.gdx.scenes.scene2d.Stage stage) {
        if (stage == null) return;
        createBackgroundIfNeeded(null);
        if (bgImage == null) return;
        if (bgImage.getStage() != stage) stage.addActor(bgImage);
        bgImage.setZIndex(Math.max(0, getZIndex() - 1));
        bgImage.setColor(1f, 1f, 1f, 1f);
        syncBackground();
    }

    public void syncBackground() {
        if (bgImage == null) return;
        bgImage.setSize(getWidth(), getHeight());
        bgImage.setPosition(getX(), getY());
    }

    private void createBackgroundIfNeeded(GameContext context) {
        if (bgImage != null) return;

        var bgFile = Gdx.files.internal(BG_PATH);
        if (!bgFile.exists()) {
            Gdx.app.log("SpliceLab", "Missing dialog background: " + bgFile.path());
            return;
        }

        Gdx.app.log("SpliceLab", "SettingsDialog loading background: " + bgFile.path());

        Texture texture = null;
        if (context != null && context.assets != null) {
            texture = context.assets.getTexture(BG_PATH);
        }
        if (texture == null) {
            texture = new Texture(bgFile);
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bgTex = texture;

        bgImage = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bgImage.setFillParent(false);
        // Settings bg is authored landscape; no rotation needed.
        bgImage.setOrigin(com.badlogic.gdx.utils.Align.center);
        bgImage.setRotation(0f);
        bgImage.setColor(1f, 1f, 1f, 1f);
        setColor(1f, 1f, 1f, 1f);
    }

    public SettingsDialog(Skin skin, GameContext context) {
        super("Settings", skin);

        // Match other dialogs: remove skin window tint; render bg via bgImage actor.
        setBackground((Drawable) null);
        getContentTable().setBackground((Drawable) null);
        getButtonTable().setBackground((Drawable) null);
        if (getStyle() != null) getStyle().background = null;

        createBackgroundIfNeeded(context);
        // Background is managed via showBackground(stage) so it can sit behind the dialog.

        UiFactory ui = new UiFactory(skin, context.audio);

        Table content = new Table();
        content.defaults().pad(10);

        content.add(makeSoundHeader(skin, ui, "Music")).left().row();
        content.add(makeVolumeRow(
                skin,
                context,
                () -> context.audio.isMusicMuted(),
                context.audio::setMusicMuted,
                () -> context.audio.getMusicVolume(),
                context.audio::setMusicVolume
        )).expandX().fillX().row();

        content.add(makeSoundHeader(skin, ui, "SFX")).left().padTop(16).row();
        content.add(makeVolumeRow(
                skin,
                context,
                () -> context.audio.isSfxMuted(),
                muted -> {
                    context.audio.setSfxMuted(muted);
                    context.audio.stopBeltLoop();
                    context.audio.startBeltLoop();
                },
                () -> context.audio.getSfxVolume(),
                volume -> {
                    context.audio.setSfxVolume(volume);
                    context.audio.stopBeltLoop();
                    context.audio.startBeltLoop();
                }
        )).expandX().fillX().row();

        getContentTable().add(content).width(480).pad(18);
        getButtonTable().clearChildren();

        setModal(false);
        setMovable(false);
        setResizable(false);
        pad(12);

        normalizeWindowSize();

        showBackground(getStage());

        syncBackground();

        // Keep style background transparent; we render bg via bgImage actor.
        if (getStyle() != null) getStyle().background = skin.newDrawable("white", new Color(0f, 0f, 0f, 0f));
    }

    private void normalizeWindowSize() {
        float vw = 540f;
        float vh = 960f;
        float w = vw * 0.90f;
        float h = vh * 0.52f;
        setSize(w, h);
    }

    private interface BoolGetter {
        boolean get();
    }

    private interface BoolSetter {
        void set(boolean v);
    }

    private interface FloatGetter {
        float get();
    }

    private interface FloatSetter {
        void set(float v);
    }

    private Actor makeVolumeRow(
            Skin skin,
            GameContext context,
            BoolGetter mutedGetter,
            BoolSetter mutedSetter,
            FloatGetter volumeGetter,
            FloatSetter volumeSetter
    ) {
        Table row = new Table();
        row.defaults().pad(6);

        CheckBox mute = new CheckBox(" Mute", skin);
        mute.setChecked(mutedGetter.get());

        Slider slider = new Slider(0f, 1f, 0.01f, false, skin);
        slider.setStyle(makeThickSliderStyle(skin));
        slider.setValue(volumeGetter.get());

        Label percent = new Label(toPercentText(slider.getValue()), skin);

        mute.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mutedSetter.set(mute.isChecked());
                context.audio.playButtonClick();
            }
        });

        slider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float v = slider.getValue();
                volumeSetter.set(v);
                percent.setText(toPercentText(v));
            }
        });

        row.add(mute).left();
        row.add(slider).expandX().fillX().height(34).minWidth(280);
        row.add(percent).width(56).right();
        return row;
    }

    private Slider.SliderStyle makeThickSliderStyle(Skin skin) {
        Slider.SliderStyle base;
        try {
            base = new Slider.SliderStyle(skin.get(Slider.SliderStyle.class));
        } catch (Exception e) {
            base = new Slider.SliderStyle();
        }

        if (sliderTrackTex == null) {
            sliderTrackTex = new Texture(makeRoundedPixmap(240, 18, 9, new Color(0.15f, 0.2f, 0.28f, 1f)));
            sliderTrackTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        if (sliderKnobTex == null) {
            sliderKnobTex = new Texture(makeRoundedPixmap(28, 28, 14, new Color(0.25f, 0.85f, 1f, 1f)));
            sliderKnobTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        base.background = new TextureRegionDrawable(new TextureRegion(sliderTrackTex));
        base.knob = new TextureRegionDrawable(new TextureRegion(sliderKnobTex));
        base.knobOver = base.knob;
        base.knobDown = base.knob;
        return base;
    }

    private Pixmap makeRoundedPixmap(int w, int h, int r, Color color) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        p.setColor(color);
        p.fillRectangle(r, 0, w - 2 * r, h);
        p.fillRectangle(0, r, w, h - 2 * r);
        p.fillCircle(r, r, r);
        p.fillCircle(w - r - 1, r, r);
        p.fillCircle(r, h - r - 1, r);
        p.fillCircle(w - r - 1, h - r - 1, r);
        return p;
    }

    private Actor makeSoundHeader(Skin skin, UiFactory ui, String text) {
        Table header = new Table();
        header.defaults().padRight(8);
        Drawable icon = loadSoundIcon();
        if (icon != null) {
            Image img = new Image(icon);
            img.setTouchable(Touchable.disabled);
            header.add(img).size(30, 30);
        }
        header.add(ui.label(text));
        return header;
    }

    private Drawable loadSoundIcon() {
        if (!Gdx.files.internal(SOUND_ICON_PATH).exists()) return null;
        if (soundIconTex == null) {
            soundIconTex = new Texture(Gdx.files.internal(SOUND_ICON_PATH));
            soundIconTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return new TextureRegionDrawable(new TextureRegion(soundIconTex));
    }

    private String toPercentText(float v) {
        int pct = Math.round(v * 100f);
        return pct + "%";
    }

    @Override
    public void hide() {
        super.hide();
        if (bgImage != null) bgImage.remove();
        bgImage = null;
        // bgTex may be owned by AssetManager; don't dispose here.
        bgTex = null;

        if (soundIconTex != null) soundIconTex.dispose();
        soundIconTex = null;

        if (sliderTrackTex != null) sliderTrackTex.dispose();
        sliderTrackTex = null;

        if (sliderKnobTex != null) sliderKnobTex.dispose();
        sliderKnobTex = null;
    }
}
