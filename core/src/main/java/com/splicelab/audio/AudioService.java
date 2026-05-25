package com.splicelab.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public final class AudioService {
    private static final String SFX_BELT_PATH = "sfx/belt.mp3";
    private static final String MUSIC_MAINPAGE_PATH = "sfx/mainpage.mp3";
    private static final String MUSIC_LAB_PATH = "sfx/labmusic.mp3";
    private static final String SFX_TUBE_CLICK_PATH = "sfx/tubeclick.mp3";
    private static final String SFX_BUTTON_CLICK_PATH = "sfx/buttonclick.mp3";
    private static final String SFX_DRAG_PATH = "sfx/drag.mp3";
    private static final String SFX_DROP_PATH = "sfx/drop.mp3";
    private static final String SFX_ENEMY_THROW_PATH = "sfx/enemythrow.mp3";
    private static final String SFX_HERO_THROW_PATH = "sfx/herothrow.mp3";
    private static final String SFX_HERO_DIES_PATH = "sfx/herodies.mp3";
    private static final String SFX_ENEMY_DIES_PATH = "sfx/enemydies.mp3";
    private static final String SFX_CRACK_PATH = "sfx/crack.mp3";
    private static final String SFX_WIN_PATH = "sfx/win.mp3";
    private static final String SFX_LOSE_PATH = "sfx/lose.mp3";
    private static final String SFX_ENEMY_APPEARS_PATH = "sfx/enemyappears.mp3";
    private static final String SFX_TIMEOUT_PATH = "sfx/timeout.mp3";

    private Music beltLoop;
    private Music mainpageLoop;
    private Music labLoop;
    private Sound tubeClick;
    private Sound buttonClick;
    private Sound drag;
    private Sound drop;
    private Sound enemyThrow;
    private Sound heroThrow;
    private Sound heroDies;
    private Sound enemyDies;
    private Sound crack;
    private Sound win;
    private Sound lose;
    private Sound enemyAppears;
    private Sound timeout;

    private float musicVolume = 0.12f;
    private float sfxVolume = 0.55f;

    private boolean musicMuted = false;
    private boolean sfxMuted = false;

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        applyMusicVolume();
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    public void setMusicMuted(boolean muted) {
        musicMuted = muted;
        applyMusicVolume();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }

    public void setSfxMuted(boolean muted) {
        sfxMuted = muted;
    }

    public void startMainpageLoop() {
        if (mainpageLoop == null) {
            if (!Gdx.files.internal(MUSIC_MAINPAGE_PATH).exists()) return;
            mainpageLoop = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_MAINPAGE_PATH));
            mainpageLoop.setLooping(true);
            applyMusicVolume();
        }
        mainpageLoop.play();
    }

    public void stopMainpageLoop() {
        if (mainpageLoop != null) mainpageLoop.stop();
    }

    public void startLabLoop() {
        if (labLoop == null) {
            if (!Gdx.files.internal(MUSIC_LAB_PATH).exists()) return;
            labLoop = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_LAB_PATH));
            labLoop.setLooping(true);
            applyMusicVolume();
        }
        labLoop.play();
    }

    public void stopLabLoop() {
        if (labLoop != null) labLoop.stop();
    }

    public void startBeltLoop() {
        if (beltLoop == null) {
            if (!Gdx.files.internal(SFX_BELT_PATH).exists()) return;
            beltLoop = Gdx.audio.newMusic(Gdx.files.internal(SFX_BELT_PATH));
            beltLoop.setLooping(true);
            applyBeltVolume();
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
        tubeClick.play(sfxMuted ? 0f : sfxVolume);
    }

    public void playButtonClick() {
        if (buttonClick == null) {
            if (!Gdx.files.internal(SFX_BUTTON_CLICK_PATH).exists()) return;
            buttonClick = Gdx.audio.newSound(Gdx.files.internal(SFX_BUTTON_CLICK_PATH));
        }
        buttonClick.play(sfxMuted ? 0f : clamp01(sfxVolume * 0.8f));
    }

    public void playDrag() {
        if (drag == null) {
            if (!Gdx.files.internal(SFX_DRAG_PATH).exists()) return;
            drag = Gdx.audio.newSound(Gdx.files.internal(SFX_DRAG_PATH));
        }
        drag.play(sfxMuted ? 0f : clamp01(sfxVolume * 0.65f));
    }

    public void playDrop() {
        if (drop == null) {
            if (!Gdx.files.internal(SFX_DROP_PATH).exists()) return;
            drop = Gdx.audio.newSound(Gdx.files.internal(SFX_DROP_PATH));
        }
        drop.play(sfxMuted ? 0f : clamp01(sfxVolume * 0.72f));
    }

    public void playEnemyThrow() {
        if (enemyThrow == null) {
            if (!Gdx.files.internal(SFX_ENEMY_THROW_PATH).exists()) return;
            enemyThrow = Gdx.audio.newSound(Gdx.files.internal(SFX_ENEMY_THROW_PATH));
        }
        enemyThrow.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playHeroThrow() {
        if (heroThrow == null) {
            if (!Gdx.files.internal(SFX_HERO_THROW_PATH).exists()) return;
            heroThrow = Gdx.audio.newSound(Gdx.files.internal(SFX_HERO_THROW_PATH));
        }
        heroThrow.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playHeroDies() {
        if (heroDies == null) {
            if (!Gdx.files.internal(SFX_HERO_DIES_PATH).exists()) return;
            heroDies = Gdx.audio.newSound(Gdx.files.internal(SFX_HERO_DIES_PATH));
        }
        heroDies.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playEnemyDies() {
        if (enemyDies == null) {
            if (!Gdx.files.internal(SFX_ENEMY_DIES_PATH).exists()) return;
            enemyDies = Gdx.audio.newSound(Gdx.files.internal(SFX_ENEMY_DIES_PATH));
        }
        enemyDies.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playCrack() {
        if (crack == null) {
            if (!Gdx.files.internal(SFX_CRACK_PATH).exists()) return;
            crack = Gdx.audio.newSound(Gdx.files.internal(SFX_CRACK_PATH));
        }
        crack.play(sfxMuted ? 0f : clamp01(sfxVolume * 0.75f));
    }

    public void playWin() {
        if (win == null) {
            if (!Gdx.files.internal(SFX_WIN_PATH).exists()) return;
            win = Gdx.audio.newSound(Gdx.files.internal(SFX_WIN_PATH));
        }
        win.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playLose() {
        if (lose == null) {
            if (!Gdx.files.internal(SFX_LOSE_PATH).exists()) return;
            lose = Gdx.audio.newSound(Gdx.files.internal(SFX_LOSE_PATH));
        }
        lose.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playEnemyAppears() {
        if (enemyAppears == null) {
            if (!Gdx.files.internal(SFX_ENEMY_APPEARS_PATH).exists()) return;
            enemyAppears = Gdx.audio.newSound(Gdx.files.internal(SFX_ENEMY_APPEARS_PATH));
        }
        enemyAppears.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    public void playTimeoutWarning() {
        if (timeout == null) {
            if (!Gdx.files.internal(SFX_TIMEOUT_PATH).exists()) return;
            timeout = Gdx.audio.newSound(Gdx.files.internal(SFX_TIMEOUT_PATH));
        }
        timeout.play(sfxMuted ? 0f : clamp01(sfxVolume));
    }

    private void applyMusicVolume() {
        float v = musicMuted ? 0f : musicVolume;
        if (mainpageLoop != null) mainpageLoop.setVolume(v);
        if (labLoop != null) labLoop.setVolume(v);
    }

    private void applyBeltVolume() {
        float v = sfxMuted ? 0f : 1f;
        if (beltLoop != null) beltLoop.setVolume(v);
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    public void dispose() {
        if (beltLoop != null) beltLoop.dispose();
        if (mainpageLoop != null) mainpageLoop.dispose();
        if (labLoop != null) labLoop.dispose();
        if (tubeClick != null) tubeClick.dispose();
        if (buttonClick != null) buttonClick.dispose();
        if (drag != null) drag.dispose();
        if (drop != null) drop.dispose();
        if (enemyThrow != null) enemyThrow.dispose();
        if (heroThrow != null) heroThrow.dispose();
        if (heroDies != null) heroDies.dispose();
        if (enemyDies != null) enemyDies.dispose();
        if (crack != null) crack.dispose();
        if (win != null) win.dispose();
        if (lose != null) lose.dispose();
        if (enemyAppears != null) enemyAppears.dispose();
        if (timeout != null) timeout.dispose();
    }
}
