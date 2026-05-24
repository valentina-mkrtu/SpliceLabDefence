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

    // Debug flag: keep OFF for now.
    // When enabled, only cleared levels + next available level are selectable.
    private static final boolean ENABLE_LEVEL_LOCK = false;

    public LevelMapDialog(Skin skin, GameContext context, LevelSelectListener listener) {
        super("Map", skin);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table list = new Table();
        list.defaults().pad(8).expandX().fillX();

        int totalLevels = 50;
        for (int level = 1; level <= totalLevels; level++) {
            boolean cleared = context.saves.get().completedLevels.contains(level);

            boolean unlocked = true;
            if (ENABLE_LEVEL_LOCK) {
                // Allow replay of cleared levels, plus the next uncleared level.
                unlocked = cleared || context.saves.get().completedLevels.contains(level - 1) || level == 1;
            }

            TextButton btn = ui.textButton("Level " + level);
            Color baseColor;
            if (!unlocked) baseColor = new Color(0.18f, 0.18f, 0.18f, 1f);
            else if (cleared) baseColor = new Color(0.25f, 0.65f, 0.35f, 1f);
            else baseColor = new Color(0.3f, 0.3f, 0.3f, 1f);

            btn.getStyle().up = skin.newDrawable("white", baseColor);
            btn.getStyle().down = skin.newDrawable(
                    "white",
                    !unlocked ? new Color(0.14f, 0.14f, 0.14f, 1f)
                            : (cleared ? new Color(0.2f, 0.55f, 0.3f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f))
            );

            btn.setDisabled(!unlocked);
            final int selected = level;
            btn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (!unlocked) return;
                    if (listener != null) listener.onSelectLevel(selected);
                    hide();
                }
            });

            list.add(btn).height(54).row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollbarsVisible(true);
        scroll.setOverscroll(false, false);
        scroll.setForceScroll(false, true);
        scroll.setFlickScroll(true);

        // Force a constrained viewport so scroll can actually happen.
        getContentTable().add(scroll).width(440).height(520).pad(10);
        button("Close");

        // Non-modal so bottom nav buttons stay clickable.
        setModal(false);
        setMovable(false);
        pad(12);
    }
}
