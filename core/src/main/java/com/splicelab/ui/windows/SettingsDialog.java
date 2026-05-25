package com.splicelab.ui.windows;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;

public final class SettingsDialog extends Dialog {
    public SettingsDialog(Skin skin, GameContext context) {
        super("Settings", skin);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table content = new Table();
        content.defaults().pad(10);

        content.add(ui.label("Music")).left().row();
        content.add(makeVolumeRow(
                skin,
                context,
                () -> context.audio.isMusicMuted(),
                context.audio::setMusicMuted,
                () -> context.audio.getMusicVolume(),
                context.audio::setMusicVolume
        )).expandX().fillX().row();

        content.add(ui.label("SFX")).left().padTop(16).row();
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

        getContentTable().add(content).width(480).pad(8);

        TextButton close = ui.textButton("Close");
        close.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                context.audio.playButtonClick();
                hide();
            }
        });
        getButtonTable().add(close);

        setModal(false);
        setMovable(false);
        setResizable(false);
        pad(12);

        normalizeWindowSize();
    }

    private void normalizeWindowSize() {
        float vw = 540f;
        float vh = 960f;
        float w = vw * 0.86f;
        float h = vh * 0.46f;
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
        row.add(slider).expandX().fillX().minWidth(260);
        row.add(percent).width(56).right();
        return row;
    }

    private String toPercentText(float v) {
        int pct = Math.round(v * 100f);
        return pct + "%";
    }
}

