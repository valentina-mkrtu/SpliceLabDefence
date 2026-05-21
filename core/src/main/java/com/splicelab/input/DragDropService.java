package com.splicelab.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public final class DragDropService {
    private final DragAndDrop dragAndDrop = new DragAndDrop();

    public DragAndDrop get() {
        return dragAndDrop;
    }

    public void clear() {
        dragAndDrop.clear();
    }

    public static Actor safeActor(Actor actor) {
        return actor;
    }
}

