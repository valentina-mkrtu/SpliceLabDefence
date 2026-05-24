package com.splicelab.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public final class AudioService {
    private static final String SFX_BELT_PATH = "sfx/belt.wav";
    private static final String SFX_TUBE_CLICK_PATH = "sfx/tubeclick.mp3";
    private static final String SFX_BUTTON_CLICK_PATH = "sfx/buttonclick.mp3";
    private static final String SFX_DRAG_PATH = "sfx/drag.mp3";
    private static final String SFX_DROP_PATH = "sfx/drop.mp3";

    private Music beltLoop;
    private Sound tubeClick;
    private Sound buttonClick;
    private Sound drag;
    private Sound drop;

    public void startBeltLoop() {
        if (beltLoop == null) {
            if (!Gdx.files.internal(SFX_BELT_PATH).exists()) return;
            beltLoop = Gdx.audio.newMusic(Gdx.files.internal(SFX_BELT_PATH));
            beltLoop.setLooping(true);
            beltLoop.setVolume(0.12f);
        }
        beltLoop.play();
    }

    public void stopBeltLoop() {
        if (beltLoop != null) beltLoop.stop();
    }

    public void playTubeClick() {
        if (tubeClick == null) {
            if (!Gdx.files.internal(SFX_TUBE_CLICK_PATH).exists()) return;
            tubeClick = Gdx.audio.newSound(Gdx.files.internal(SFX_TUBE_CLICK_PATH));
        }
        tubeClick.play(0.55f);
    }

    public void playButtonClick() {
        if (buttonClick == null) {
            if (!Gdx.files.internal(SFX_BUTTON_CLICK_PATH).exists()) return;
            buttonClick = Gdx.audio.newSound(Gdx.files.internal(SFX_BUTTON_CLICK_PATH));
        }
        buttonClick.play(0.45f);
    }

    public void playDrag() {
        if (drag == null) {
            if (!Gdx.files.internal(SFX_DRAG_PATH).exists()) return;
            drag = Gdx.audio.newSound(Gdx.files.internal(SFX_DRAG_PATH));
        }
        drag.play(0.35f);
    }

    public void playDrop() {
        if (drop == null) {
            if (!Gdx.files.internal(SFX_DROP_PATH).exists()) return;
            drop = Gdx.audio.newSound(Gdx.files.internal(SFX_DROP_PATH));
        }
        drop.play(0.40f);
    }

    public void dispose() {
        if (beltLoop != null) beltLoop.dispose();
        if (tubeClick != null) tubeClick.dispose();
        if (buttonClick != null) buttonClick.dispose();
        if (drag != null) drag.dispose();
        if (drop != null) drop.dispose();
    }
}
