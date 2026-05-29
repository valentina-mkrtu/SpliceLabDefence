package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.splicelab.app.GameContext;
import com.splicelab.combat.MidLevelBuff;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.UiStyle;

/**
 * Modal dialog shown at the mid-level checkpoint offering the player two buffs.
 * Dismisses only when the player taps one of the two options.
 */
public final class MidLevelChoiceDialog extends Dialog {

    public interface ChoiceListener {
        void onChosen(MidLevelBuff chosen);
    }

    public MidLevelChoiceDialog(Skin skin, GameContext ctx,
                                 MidLevelBuff optionA, MidLevelBuff optionB,
                                 ChoiceListener listener) {
        super("", skin);

        setModal(true);
        setMovable(false);

        if (getStyle() != null) {
            getStyle().background = skin.newDrawable("white", UiStyle.PANEL_DARK);
        }

        UiFactory ui = new UiFactory(skin, ctx.audio);

        Table content = getContentTable();
        content.defaults().pad(12).expandX().fillX();

        var header = ui.label("MID-LEVEL BONUS");
        header.setFontScale(1.3f);
        header.setAlignment(Align.center);
        content.add(header).row();

        var sub = ui.smallLabel("Choose one buff for the rest of this level:");
        sub.setAlignment(Align.center);
        content.add(sub).padBottom(8).row();

        content.add(makeOptionButton(skin, ui, ctx, optionA, listener)).height(90).row();
        content.add(makeOptionButton(skin, ui, ctx, optionB, listener)).height(90).padBottom(8).row();

        getButtonTable().clearChildren();
        pack();
        setWidth(Math.max(getWidth(), 400f));
    }

    private TextButton makeOptionButton(Skin skin, UiFactory ui, GameContext ctx,
                                        MidLevelBuff buff, ChoiceListener listener) {
        Table inner = new Table();
        inner.defaults().left().pad(4);

        var nameLabel = ui.label(buff.displayName);
        nameLabel.setFontScale(0.95f);
        nameLabel.setColor(UiStyle.DNA_ACCENT);
        inner.add(nameLabel).row();

        var descLabel = ui.smallLabel(buff.description);
        descLabel.setFontScale(0.75f);
        descLabel.setWrap(true);
        inner.add(descLabel).expandX().fillX().row();

        TextButton btn = new TextButton("", skin);
        btn.clearChildren();
        btn.add(inner).grow().pad(12);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                ctx.audio.playButtonClick();
                if (listener != null) listener.onChosen(buff);
                hide();
            }
        });
        return btn;
    }
}
