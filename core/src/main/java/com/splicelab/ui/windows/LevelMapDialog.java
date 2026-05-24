package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.splicelab.app.GameContext;
import com.splicelab.ui.UiFactory;

public final class LevelMapDialog extends Dialog {
    public interface LevelSelectListener {
        void onSelectLevel(int levelNumber);
    }

    public LevelMapDialog(Skin skin, GameContext context, LevelSelectListener listener) {
        super("Map", skin);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table list = new Table();
        list.defaults().pad(8).expandX().fillX();

        int totalLevels = 50;
        for (int level = 1; level <= totalLevels; level++) {
            boolean cleared = context.saves.get().completedLevels.contains(level);

            TextButton btn = ui.textButton("Level " + level);
            btn.getStyle().up = skin.newDrawable("white", cleared ? new Color(0.25f, 0.65f, 0.35f, 1f) : new Color(0.3f, 0.3f, 0.3f, 1f));
            btn.getStyle().down = skin.newDrawable("white", cleared ? new Color(0.2f, 0.55f, 0.3f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f));
            final int selected = level;
            btn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (listener != null) listener.onSelectLevel(selected);
                    hide();
                }
            });

            list.add(btn).height(54).row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        // ~10 levels visible at once.
        getContentTable().add(scroll).width(440).height(10 * 62).pad(10);
        button("Close");

        setModal(true);
        setMovable(false);
        pad(12);
    }
}
