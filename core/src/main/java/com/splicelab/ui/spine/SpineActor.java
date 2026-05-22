package com.splicelab.ui.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

// Placeholder wrapper.
// If Spine runtime is not on classpath, this actor renders nothing but keeps game stable.
public final class SpineActor extends Actor {
    private final String atlasPath;
    private final String skelPath;
    private final String animationName;

    public SpineActor(String atlasPath, String skelPath, String animationName) {
        this.atlasPath = atlasPath;
        this.skelPath = skelPath;
        this.animationName = animationName;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // TODO: Implement with Spine 4.3 runtime.
        // For now we log once if used.
        if (Gdx.app != null) {
            Gdx.app.debug("SpliceLab", "SpineActor placeholder used atlas=" + atlasPath + " skel=" + skelPath + " anim=" + animationName);
        }
    }
}

