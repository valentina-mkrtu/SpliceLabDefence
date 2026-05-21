package com.splicelab.ui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameContext;
import com.splicelab.assets.PlaceholderSkinFactory;
import com.splicelab.combat.CombatController;
import com.splicelab.combat.CombatState;
import com.splicelab.combat.CombatTuning;
import com.splicelab.model.IngredientKind;
import com.splicelab.model.enemy.EnemyDefinition;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.ingredient.SimpleIngredientInstance;
import com.splicelab.ui.UiConstants;
import com.splicelab.ui.UiFactory;
import com.splicelab.ui.widgets.GridCellWidget;
import com.splicelab.ui.widgets.HpBarWidget;
import com.splicelab.ui.widgets.LevelTimerWidget;
import com.splicelab.ui.widgets.TubeWidget;

public final class LabGameView {
    private final GameContext context;
    private final Skin skin;
    private final UiFactory ui;

    private final Table root;
    private final GridCellWidget[][] cells = new GridCellWidget[AppConstants.GRID_COLS][AppConstants.GRID_ROWS];
    private final TubeWidget tube;
    // 12-socket conveyor prototype.
    private static final int SOCKET_COUNT = 12;
    private final Table[] conveyorSockets;
    private final int[] socketPathIndex;
    private final FusionInstance[] socketFusion;
    private final LevelTimerWidget timer;

    private final Label tubeStatus;
    private final HpBarWidget tubeHpBar;

    private final Label enemyLabel;
    private final HpBarWidget enemyHpBar;
    private final Table enemyVisual;

    private final Table attackZoneMarker;

    private Runnable onTubeTapped;

    private final DragAndDrop dragAndDrop = new DragAndDrop();

    private final Table beltLayer;
    private final Table beltLoop;
    private final Table[] pathAnchors;

    public LabGameView(GameContext context) {
        this.context = context;
        this.skin = PlaceholderSkinFactory.create();
        this.ui = new UiFactory(skin);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));

        Table top = ui.panel();
        top.add(ui.label("Combat Area (prototype)")).pad(8).left();
        tubeStatus = ui.label("Tube");
        top.add(tubeStatus).pad(8).left();
        timer = new LevelTimerWidget(skin, ui);
        top.add(timer).expandX().right().pad(8);

        tubeHpBar = new HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.95f, 0.2f, 0.2f, 1f));
        tubeHpBar.setSize(180, 10);
        top.add(tubeHpBar).pad(8).left();

        Table conveyor = ui.panel();
        conveyor.add(ui.label("Conveyor / Vent"))
                .pad(8)
                .row();

        Table enemyPanel = new Table();
        enemyPanel.add(ui.label("ENEMY")).row();
        enemyLabel = ui.label("-");
        enemyPanel.add(enemyLabel).pad(4).row();
        enemyHpBar = new HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.95f, 0.2f, 0.2f, 1f));
        enemyHpBar.setSize(180, 10);
        enemyPanel.add(enemyHpBar).pad(4);

        enemyVisual = new Table();
        enemyVisual.setBackground(skin.newDrawable("white", new Color(0.25f, 0.25f, 0.3f, 1f)));
        enemyVisual.setSize(140, 90);
        enemyPanel.row();
        enemyPanel.add(enemyVisual).size(140, 90).pad(6);

        conveyor.add(enemyPanel).pad(6);

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

        // Conveyor belt layer: path anchors + moving sockets.
        beltLayer = new Table();
        beltLayer.setFillParent(true);
        beltLayer.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        root.addActor(beltLayer);

        // Visible placeholder conveyor loop.
        beltLoop = new Table();
        beltLoop.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        beltLayer.addActor(beltLoop);

        pathAnchors = new Table[SOCKET_COUNT];
        for (int i = 0; i < pathAnchors.length; i++) {
            pathAnchors[i] = makePathAnchor();
            beltLayer.addActor(pathAnchors[i]);
        }

        conveyorSockets = new Table[SOCKET_COUNT];
        socketPathIndex = new int[SOCKET_COUNT];
        socketFusion = new FusionInstance[SOCKET_COUNT];
        for (int i = 0; i < SOCKET_COUNT; i++) {
            conveyorSockets[i] = makeSocket(i);
            socketPathIndex[i] = i;
            beltLayer.addActor(conveyorSockets[i]);
        }

        layoutConveyorPath();

        attackZoneMarker = new Table();
        attackZoneMarker.setBackground(skin.newDrawable("white", new Color(1f, 1f, 0.2f, 0.55f)));
        attackZoneMarker.setSize(22, 22);
        attackZoneMarker.add(ui.label("ATTACK")).pad(2);
        root.addActor(attackZoneMarker);
        positionAttackZoneMarker();
    }

    private Table makePathAnchor() {
        Table t = new Table();
        t.setSize(10, 10);
        t.setVisible(false);
        return t;
    }

    private Table makeSocket(int index) {
        Table t = new Table();
        t.setSize(44, 44);
        t.setBackground(skin.newDrawable("white", new Color(0.16f, 0.17f, 0.2f, 0.95f)));
        Label lbl = ui.label(String.valueOf(index));
        lbl.setColor(new Color(1f, 1f, 1f, 0.65f));
        t.add(lbl);
        return t;
    }

    private void layoutConveyorPath() {
        // Fixed 12-point loop in beltLayer coordinates.
        // Keep it stable: do not depend on child actor layout/initialization.
        float margin = 70f;
        float combatTopY = root.getHeight() - 70f;
        float combatBottomY = root.getHeight() - 330f;
        float leftX = margin;
        float rightX = root.getWidth() - margin;

        float topY = combatTopY;
        float bottomY = combatBottomY;
        float midY = (topY + bottomY) / 2f;
        float midX = (leftX + rightX) / 2f;

        Vector2[] pts = new Vector2[]{
                // top row (L -> C -> R)
                new Vector2(leftX, topY),
                new Vector2(midX, topY),
                new Vector2(rightX, topY),
                // right side (T -> M -> B)
                new Vector2(rightX, (topY + midY) / 2f),
                new Vector2(rightX, midY),
                new Vector2(rightX, (midY + bottomY) / 2f),
                // bottom row (R -> C -> L)
                new Vector2(rightX, bottomY),
                new Vector2(midX, bottomY),
                new Vector2(leftX, bottomY),
                // left side (B -> M -> T)
                new Vector2(leftX, (midY + bottomY) / 2f),
                new Vector2(leftX, midY),
                new Vector2(leftX, (topY + midY) / 2f)
        };

        // Visible belt loop background around the points.
        float loopPad = 50f;
        beltLoop.setSize((rightX - leftX) + loopPad * 2f, (topY - bottomY) + loopPad * 2f);
        beltLoop.setPosition(leftX - loopPad, bottomY - loopPad);
        beltLoop.setBackground(makeLoopDrawable());

        for (int i = 0; i < pts.length; i++) {
            pathAnchors[i].setPosition(pts[i].x, pts[i].y);
        }

        // Place sockets at their current path indices.
        for (int i = 0; i < conveyorSockets.length; i++) {
            setSocketToPathIndex(i, socketPathIndex[i], false);
        }
    }

    private void setSocketToPathIndex(int socketIndex, int pathIndex, boolean animate) {
        Actor anchor = getConveyorAnchor(pathIndex);
        if (anchor == null) return;
        float x = anchor.getX() - conveyorSockets[socketIndex].getWidth() / 2f;
        float y = anchor.getY() - conveyorSockets[socketIndex].getHeight() / 2f;
        if (!animate) {
            conveyorSockets[socketIndex].setPosition(x, y);
        } else {
            conveyorSockets[socketIndex].clearActions();
            conveyorSockets[socketIndex].addAction(Actions.moveTo(x, y, CombatTuning.CONVEYOR_MOVE_DURATION_SECONDS));
        }
    }

    private Drawable makeLoopDrawable() {
        // Dark belt with slightly lighter inner to suggest a track.
        return skin.newDrawable("white", new Color(0.08f, 0.09f, 0.11f, 0.85f));
    }

    private void positionAttackZoneMarker() {
        Actor anchor = getConveyorAnchor(CombatTuning.ATTACK_ZONE_INDEX);
        if (anchor == null) return;
        Vector2 p = anchor.localToStageCoordinates(new Vector2(anchor.getWidth() / 2f, anchor.getHeight() / 2f));
        attackZoneMarker.setPosition(p.x - attackZoneMarker.getWidth() / 2f, p.y - attackZoneMarker.getHeight() / 2f);
    }

    public Actor getRoot() {
        return root;
    }

    public void setOnTubeTapped(Runnable onTubeTapped) {
        this.onTubeTapped = onTubeTapped;
    }

    public void bindDragDrop(CombatController controller) {
        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                if (c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW) continue;
                final GridCellWidget cell = cells[c][r];
                dragAndDrop.addSource(new GridCellSource(cell));
                dragAndDrop.addTarget(new GridCellTarget(cell, controller));
            }
        }

        for (int i = 0; i < conveyorSockets.length; i++) {
            dragAndDrop.addTarget(new ConveyorSocketTarget(conveyorSockets[i], i, controller));
        }
    }

    public void syncFromState(CombatState state) {
        if (state == null) return;
        layoutConveyorPath();
        positionAttackZoneMarker();
        timer.setSeconds(state.remainingTimeSeconds);
        tubeStatus.setText("Tube HP " + state.tubeHp + " | CD " + String.format("%.1f", state.tubeCooldownRemaining) + " | Charges " + state.tubeCharges);

        float tubePct = state.level == null ? 1f : (state.tubeHp / (float) Math.max(1, state.level.tubeHp));
        tubeHpBar.setPercent(tubePct);

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

        // Socket visuals show deployed fusions.
        for (int i = 0; i < socketFusion.length; i++) socketFusion[i] = null;
        for (int i = 0; i < state.conveyorLeft.length; i++) {
            FusionInstance f = state.conveyorLeft[i];
            if (f == null) continue;
            int idx = state.conveyorPathIndexLeft[i];
            if (idx >= 0 && idx < SOCKET_COUNT) socketFusion[idx] = f;
        }
        for (int i = 0; i < state.conveyorRight.length; i++) {
            FusionInstance f = state.conveyorRight[i];
            if (f == null) continue;
            int idx = state.conveyorPathIndexRight[i];
            if (idx >= 0 && idx < SOCKET_COUNT) socketFusion[idx] = f;
        }

        for (int i = 0; i < conveyorSockets.length; i++) {
            conveyorSockets[i].clearChildren();
            String label = socketFusion[i] == null ? String.valueOf(i) : ("F\n" + socketFusion[i].displayName);
            Label lbl = ui.label(label);
            lbl.setAlignment(com.badlogic.gdx.utils.Align.center);
            lbl.setColor(new Color(1f, 1f, 1f, socketFusion[i] == null ? 0.65f : 1f));
            conveyorSockets[i].add(lbl).grow();
        }

        if (state.activeEnemy == null) {
            enemyLabel.setText("-");
            enemyHpBar.setPercent(0f);
            enemyVisual.setVisible(false);
        } else {
            EnemyDefinition def = context.definitions.getEnemy(state.activeEnemy.enemyType).orElse(null);
            enemyLabel.setText(def == null ? state.activeEnemy.enemyType.name() : def.displayName);
            int maxHp = def == null ? Math.max(1, state.activeEnemy.hp) : Math.max(1, Math.round(def.maxHp * state.level.enemyHpMultiplier));
            enemyHpBar.setPercent(state.activeEnemy.hp / (float) maxHp);
            enemyVisual.setVisible(true);
        }
    }

    public void spawnProjectile(Actor from, Actor to, Color color, Runnable onHit) {
        if (from == null || to == null) return;
        Table p = new Table();
        p.setBackground(skin.newDrawable("white", color));
        p.setSize(10, 10);
        root.addActor(p);

        Vector2 start = from.localToStageCoordinates(new Vector2(from.getWidth() / 2f, from.getHeight() / 2f));
        Vector2 end = to.localToStageCoordinates(new Vector2(to.getWidth() / 2f, to.getHeight() / 2f));
        float startX = start.x;
        float startY = start.y;
        float endX = end.x;
        float endY = end.y;
        p.setPosition(startX, startY);

        float dist = (float) Math.hypot(endX - startX, endY - startY);
        float dur = Math.max(0.08f, dist / CombatTuning.PROJECTILE_SPEED_PX_PER_SEC);
        p.addAction(Actions.sequence(
                Actions.moveTo(endX, endY, dur),
                Actions.run(() -> {
                    if (onHit != null) onHit.run();
                }),
                Actions.removeActor()
        ));
    }

    public void floatTextNear(Actor anchor, String text, Color color) {
        if (anchor == null) return;
        Label lbl = ui.label(text);
        lbl.setColor(color);
        root.addActor(lbl);
        Vector2 pos = anchor.localToStageCoordinates(new Vector2(anchor.getWidth() / 2f, anchor.getHeight() / 2f));
        lbl.setPosition(pos.x, pos.y);
        lbl.getColor().a = 1f;
        lbl.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(0f, 36f, 0.6f),
                        Actions.fadeOut(0.6f)
                ),
                Actions.removeActor()
        ));
    }

    public Actor getEnemyAnchor() {
        return enemyVisual;
    }

    public Actor getTubeAnchor() {
        return tube;
    }

    public Actor getConveyorSlotAnchor(boolean leftSide, int index) {
        // Slot actors are no longer used for the conveyor prototype.
        return null;
    }

    public int getConveyorPathLength() {
        return SOCKET_COUNT;
    }

    public Actor getConveyorAnchor(int pathIndex) {
        if (pathIndex < 0 || pathIndex >= pathAnchors.length) return null;
        return pathAnchors[pathIndex];
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

    private final class GridCellSource extends DragAndDrop.Source {
        private final GridCellWidget cell;

        public GridCellSource(GridCellWidget cell) {
            super(cell);
            this.cell = cell;
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(cell);
            Label dragLabel = ui.label(cell.getLabelText());
            payload.setDragActor(dragLabel);
            return payload;
        }
    }

    private final class GridCellTarget extends DragAndDrop.Target {
        private final GridCellWidget target;
        private final CombatController controller;

        public GridCellTarget(GridCellWidget actor, CombatController controller) {
            super(actor);
            this.target = actor;
            this.controller = controller;
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            return payload.getObject() instanceof GridCellWidget;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            if (!(payload.getObject() instanceof GridCellWidget from)) return;
            controller.requestMoveOrFuse(from.col, from.row, target.col, target.row);
        }
    }

    private final class ConveyorSocketTarget extends DragAndDrop.Target {
        private final int socketIndex;
        private final CombatController controller;

        public ConveyorSocketTarget(Actor actor, int socketIndex, CombatController controller) {
            super(actor);
            this.socketIndex = socketIndex;
            this.controller = controller;
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            return payload.getObject() instanceof GridCellWidget;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            if (!(payload.getObject() instanceof GridCellWidget from)) return;
            // Map socket index to controller slot (left side uses 0..2, right side uses 0..2).
            // To keep prototype playable, use all 12 sockets as valid indices by spreading across slots.
            boolean leftSide = socketIndex < 6;
            int slot = Math.min(2, socketIndex % 3);
            controller.requestDeployFusionFromGrid(from.col, from.row, leftSide, slot);
        }
    }
}
