package com.splicelab.ui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.combat.CombatController;
import com.splicelab.combat.CombatState;
import com.splicelab.input.DragDropService;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.ingredient.SimpleIngredientInstance;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.widgets.ConveyorSlotWidget;
import com.splicelab.ui.widgets.GridCellWidget;
import com.splicelab.ui.widgets.LevelTimerWidget;
import com.splicelab.ui.widgets.TubeWidget;

public final class LabGameView {
    private final GameContext context;
    private final Skin skin;
    private final UiFactory ui;

    private final Table root;
    private final GridCellWidget[][] cells = new GridCellWidget[AppConstants.GRID_COLS][AppConstants.GRID_ROWS];
    private final TubeWidget tube;
    private final ConveyorSlotWidget[] leftSlots;
    private final ConveyorSlotWidget[] rightSlots;
    private final LevelTimerWidget timer;

    private Runnable onTubeTapped;

    private final DragDropService dragDropService = new DragDropService();

    public LabGameView(GameContext context) {
        this.context = context;
        this.skin = PlaceholderSkinFactory.create();
        this.ui = new UiFactory(skin);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        Table top = ui.panel();
        top.add(ui.label("Combat Area (prototype)")).pad(8).left();
        timer = new LevelTimerWidget(skin, ui);
        top.add(timer).expandX().right().pad(8);

        Table conveyor = ui.panel();
        conveyor.add(ui.label("Conveyor / Vent"))
                .colspan(3)
                .pad(8)
                .row();

        leftSlots = new ConveyorSlotWidget[context.config.maxConveyorSlotsPerSide];
        rightSlots = new ConveyorSlotWidget[context.config.maxConveyorSlotsPerSide];
        Table slotsTable = new Table();
        Table left = new Table();
        Table right = new Table();
        for (int i = 0; i < leftSlots.length; i++) {
            leftSlots[i] = new ConveyorSlotWidget(skin, ui, true, i);
            rightSlots[i] = new ConveyorSlotWidget(skin, ui, false, i);
            left.add(leftSlots[i]).size(120, 70).pad(4).row();
            right.add(rightSlots[i]).size(120, 70).pad(4).row();
        }
        slotsTable.add(left).pad(6);
        slotsTable.add(ui.label("(enemy here)"));
        slotsTable.add(right).pad(6);
        conveyor.add(slotsTable).pad(6);

        Table gridPanel = ui.panel();
        gridPanel.add(ui.label("Lab Grid"))
                .colspan(AppConstants.GRID_COLS)
                .pad(6)
                .row();
        Table grid = new Table();
        for (int r = AppConstants.GRID_ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                GridCellWidget cell = new GridCellWidget(skin, ui, c, r);
                cells[c][r] = cell;
                grid.add(cell).size(110, 110).pad(4);
            }
            grid.row();
        }

        tube = new TubeWidget(skin, ui);
        cells[AppConstants.TUBE_COL][AppConstants.TUBE_ROW].addActor(tube);
        tube.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onTubeTapped != null) onTubeTapped.run();
            }
        });

        gridPanel.add(grid).pad(6);

        root.add(top).growX().height(60).pad(UiConstants.PAD).row();
        root.add(conveyor).growX().height(360).pad(UiConstants.PAD).row();
        root.add(gridPanel).grow().pad(UiConstants.PAD);
    }

    public Actor getRoot() {
        return root;
    }

    public void setOnTubeTapped(Runnable onTubeTapped) {
        this.onTubeTapped = onTubeTapped;
    }

    public void bindDragDrop(CombatController controller) {
        // Minimal prototype: tap-to-fuse by dropping one cell onto another.
        // Scene2D DragAndDrop wiring will be expanded in next tasks.
        root.addListener(new ClickListener() {
            private int pendingCol = -1;
            private int pendingRow = -1;

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Actor hit = root.hit(x, y, true);
                GridCellWidget cell = findCellAncestor(hit);
                if (cell == null) return;
                if (cell.col == AppConstants.TUBE_COL && cell.row == AppConstants.TUBE_ROW) return;

                if (pendingCol == -1) {
                    pendingCol = cell.col;
                    pendingRow = cell.row;
                } else {
                    controller.requestFuse(pendingCol, pendingRow, cell.col, cell.row);
                    pendingCol = -1;
                    pendingRow = -1;
                }
            }
        });
    }

    public void syncFromState(CombatState state) {
        if (state == null) return;
        timer.setSeconds(state.remainingTimeSeconds);

        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                GridCellWidget cell = cells[c][r];
                if (c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW) {
                    cell.setLabel("TUBE");
                    continue;
                }
                IngredientInstance inst = state.grid[c][r];
                cell.setLabel(labelFor(inst));
            }
        }

        for (int i = 0; i < leftSlots.length; i++) {
            boolean unlocked = context.unlocks.isConveyorSlotUnlocked(true, i);
            leftSlots[i].setLocked(!unlocked);
            FusionInstance f = state.conveyorLeft[i];
            leftSlots[i].setText(unlocked ? (f == null ? "Empty" : f.displayName) : "Locked");
        }
        for (int i = 0; i < rightSlots.length; i++) {
            boolean unlocked = context.unlocks.isConveyorSlotUnlocked(false, i);
            rightSlots[i].setLocked(!unlocked);
            FusionInstance f = state.conveyorRight[i];
            rightSlots[i].setText(unlocked ? (f == null ? "Empty" : f.displayName) : "Locked");
        }
    }

    private static String labelFor(IngredientInstance inst) {
        if (inst == null) return "";
        if (inst instanceof FusionInstance f) {
            return "FUSION\n" + f.displayName;
        }
        if (inst instanceof SimpleIngredientInstance s) {
            if (s.kind() == IngredientKind.ENTITY) return "ENT\n" + s.entityType().name();
            if (s.kind() == IngredientKind.ITEM) return "ITEM\n" + s.itemType().name();
        }
        return inst.kind().name();
    }

    private GridCellWidget findCellAncestor(Actor actor) {
        Actor a = actor;
        while (a != null) {
            if (a instanceof GridCellWidget gc) return gc;
            a = a.getParent();
        }
        return null;
    }
}
