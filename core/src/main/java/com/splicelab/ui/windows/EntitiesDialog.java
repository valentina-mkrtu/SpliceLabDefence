package com.splicelab.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.splicelab.app.GameContext;
import com.splicelab.ui.Scene2dPlaceholders;
import com.splicelab.ui.UiFactory;

public final class EntitiesDialog extends Dialog {
    public EntitiesDialog(Skin skin, GameContext context) {
        super("Entities", skin);

        UiFactory ui = new UiFactory(skin, context.audio);

        Table list = new Table();
        list.defaults().pad(10).expandX().fillX();

        list.add(makeCard(skin, ui, "Slime", 20, 4, "Splits on hit")).row();
        list.add(makeCard(skin, ui, "Mech", 35, 6, "Armor: reduces damage")).row();
        list.add(makeCard(skin, ui, "Fungi", 22, 3, "Spore: poison over time")).row();
        list.add(makeCard(skin, ui, "Plasma", 18, 8, "Chain lightning")).row();
        list.add(makeCard(skin, ui, "Alien", 26, 5, "Mind shock: stun chance")).row();
        list.add(makeCard(skin, ui, "Egg", 12, 2, "Hatches after turns")).row();

        list.add(makeCard(skin, ui, "Battery", 0, 0, "+Energy on merge")).row();
        list.add(makeCard(skin, ui, "Toxic Waste", 0, 0, "Adds poison stacks")).row();
        list.add(makeCard(skin, ui, "Water Bottle", 0, 0, "Heals tube")).row();
        list.add(makeCard(skin, ui, "Lamp", 0, 0, "Reveals hidden bonus")).row();

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        getContentTable().add(scroll).width(460).height(540).pad(10);
        button("Close");

        setModal(true);
        setMovable(false);
        pad(12);
    }

    private Table makeCard(Skin skin, UiFactory ui, String name, int hp, int atk, String ability) {
        Table card = new Table();
        card.setBackground(skin.newDrawable("white", new Color(0.14f, 0.15f, 0.2f, 1f)));
        card.defaults().pad(6);

        Image icon = Scene2dPlaceholders.coloredSquare(skin, new Color(0.3f, 0.45f, 0.7f, 1f));
        Table left = new Table();
        left.add(icon).size(56);

        Table right = new Table();
        right.defaults().left();
        right.add(ui.label(name)).row();
        right.add(ui.label("HP: " + hp + "   ATK: " + atk)).row();
        right.add(ui.label(ability));

        card.add(left).padRight(8);
        card.add(right).expandX().fillX();
        return card;
    }
}
