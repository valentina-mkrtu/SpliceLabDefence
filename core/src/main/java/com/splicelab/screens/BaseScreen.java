package com.splicelab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.debug.DebugFlags;

public abstract class BaseScreen implements Screen {
    protected final SpliceLabGame game;
    protected final GameContext context;
    protected final Viewport viewport;
    protected final Stage stage;

    private boolean built;

    private float threeFingerHeld;

    protected BaseScreen(SpliceLabGame game, GameContext context) {
        this.game = game;
        this.context = context;
        this.viewport = new FitViewport(540, 960);
        this.stage = new Stage(viewport, game.getBatch());
    }

    protected abstract void buildUi();

    protected void update(float delta) {
    }

    protected void onPauseScreen() {
        // T-2.5: flush any pending save mutations when the screen loses focus.
        if (context != null && context.saves != null) {
            context.saves.flushIfDirty();
        }
    }

    protected void onResumeScreen() {
    }

    @Override
    public final void show() {
        Gdx.input.setInputProcessor(stage);
        if (!built) {
            built = true;
            buildUi();
        }
        onResumeScreen();
    }

    @Override
    public final void render(float delta) {
        delta = Math.min(delta, 1f / 20f);

        detectDevGesture(delta);
        update(delta);

        Gdx.gl.glClearColor(0.06f, 0.06f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    private void detectDevGesture(float delta) {
        if (!DebugFlags.DEBUG) return;
        boolean three = Gdx.input.isTouched(0)
                && Gdx.input.isTouched(1)
                && Gdx.input.isTouched(2);
        threeFingerHeld = three ? threeFingerHeld + delta : 0f;
        if (threeFingerHeld > 0.4f) {
            threeFingerHeld = -2f;
            openDevPanel();
        }
    }

    protected void openDevPanel() {
        new com.splicelab.ui.windows.DevPanelDialog(devSkin(), context).show(stage);
    }

    protected Skin devSkin() {
        if (context != null && context.skin != null) return context.skin;
        return PlaceholderSkinFactory.create();
    }

    @Override
    public final void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            Gdx.app.log(AppConstants.LOG_TAG, "Ignoring resize to " + width + "x" + height);
            return;
        }
        viewport.update(width, height, true);
    }

    @Override
    public final void pause() {
        onPauseScreen();
    }

    @Override
    public final void resume() {
        onResumeScreen();
    }

    @Override
    public void hide() {
        onPauseScreen();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
