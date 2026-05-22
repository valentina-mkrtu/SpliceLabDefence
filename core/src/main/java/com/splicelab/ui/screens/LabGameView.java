package com.splicelab.ui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private static final String CONVEYOR_LOOP_BASE_TEXTURE_PATH = "spine/production-line/belt.png";
    private static final String CONVEYOR_LOOP_LINE_TEXTURE_PATH = "spine/production-line/belt_line.png";
    private static final String SHAFT_BG_TEXTURE_PATH = "art/backgrounds/shaft.png";
    // Centerline of the dark belt lane (kept away from yellow frame).
    private static final float BELT_TRACK_INSET_X_RATIO = 0.1f;
    private static final float BELT_TRACK_INSET_Y_RATIO = 0.078f;
    private static final float BELT_TRACK_CORNER_RADIUS_RATIO = 0.17f;
    // Positive value shifts the whole belt-line segment upward.
    private static final float BELT_TRACK_VERTICAL_OFFSET_RATIO = 0.01f;

    // Marker is a fixed warning pointer on the left side.
    private static final int ATTACK_MARKER_PATH_INDEX = 10;
    // Combat attack checkpoint should match marker location.
    private static final int ATTACK_ZONE_PATH_INDEX = ATTACK_MARKER_PATH_INDEX;

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
    private final float[] pathDirectionDegrees;
    private final FusionInstance[] socketFusion;
    private final LevelTimerWidget timer;

    private final Label tubeStatus;
    private final HpBarWidget tubeHpBar;

    private final Label enemyLabel;
    private final HpBarWidget enemyHpBar;
    private final Table enemyVisual;
    private final Texture shaftBgTexture;
    private final Table shaftBg;
    private final Texture enemyReg1Texture;
    private final Texture enemyReg2Texture;
    private final Texture enemyReg3Texture;
    private final Texture enemyBoss1Texture;
    private final Texture enemyBoss2Texture;
    private final Image enemyIcon;

    private final Table attackZoneMarker;
    private final AttackZoneMarkerActor attackZoneMarkerActor;

    private float beltPhase;

    private Runnable onTubeTapped;

    private final DragAndDrop dragAndDrop = new DragAndDrop();

    private final Table beltLayer;
    private final Table beltLoop;
    private final Texture beltLoopBaseTexture;
    private final Texture beltLoopLineTexture;
    private final MovingBeltLineActor beltLoopLineActor;
    private final Table[] pathAnchors;

    private final Table background;

    private static final float FUSION_FRAME_NATIVE_W = 339f;
    private static final float FUSION_FRAME_NATIVE_H = 344f;
    private static final float FUSION_FRAME_SCALE = 1.18f;
    private static final float FUSION_FRAME_W = FUSION_FRAME_NATIVE_W * FUSION_FRAME_SCALE;
    private static final float FUSION_FRAME_H = FUSION_FRAME_NATIVE_H * FUSION_FRAME_SCALE;

    private final Table gridPanel;

    public LabGameView(GameContext context) {
        this.context = context;
        this.skin = PlaceholderSkinFactory.create();
        this.ui = new UiFactory(skin);

        PlaceholderSkinFactory.addTextureIfPresent(skin, "lab_game_bg", "art/backgrounds/lab_game_bg.png");
        PlaceholderSkinFactory.addTextureIfPresent(skin, "fusion_station_bg", "art/backgrounds/fusion_station.png");

        root = new Table();
        root.setFillParent(true);
        // Background layer should sit behind all gameplay/UI.
        background = new Table();
        background.setFillParent(true);
        var bg = PlaceholderSkinFactory.getStretchedDrawableIfPresent(skin, "lab_game_bg");
        if (bg != null) {
            background.setBackground(bg);
        } else {
            background.setBackground(skin.newDrawable("white", UiConstants.PANEL_BG));
        }
        root.addActor(background);

        // Upper HUD should not paint a panel background; let the gameplay background show through.
        Table top = new Table();
        tubeStatus = ui.label("");
        top.add(tubeStatus).pad(8).left();
        timer = new LevelTimerWidget(skin, ui);
        top.add(timer).expandX().right().pad(8);

        tubeHpBar = new HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.95f, 0.2f, 0.2f, 1f));
        tubeHpBar.setSize(180, 10);
        top.add(tubeHpBar).pad(8).left();

        // Middle section should not paint a panel background; let the gameplay background show through.
        Table conveyor = new Table();

        Table enemyPanel = new Table();
        enemyLabel = ui.label("-");
        enemyLabel.setVisible(false);
        enemyPanel.add(enemyLabel).pad(0).row();
        enemyHpBar = new HpBarWidget(skin, new Color(0f, 0f, 0f, 0.35f), new Color(0.95f, 0.2f, 0.2f, 1f));
        enemyHpBar.setSize(180, 10);

        shaftBgTexture = new Texture(SHAFT_BG_TEXTURE_PATH);

        enemyVisual = new Table();
        enemyIcon = new Image();
        enemyIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        enemyVisual.setSize(140, 90);

        shaftBg = new Table();
        shaftBg.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        shaftBg.setBackground(new TextureRegionDrawable(new TextureRegion(shaftBgTexture)));
        shaftBg.setFillParent(false);
        shaftBg.getColor().a = 1f;
        root.addActor(shaftBg);

        enemyIcon.setSize(225, 168);
        enemyIcon.setPosition(-21f, 55f);
        enemyVisual.addActor(enemyIcon);

        // Keep enemy HP bar above the enemy image.
        enemyHpBar.setPosition(0f, enemyVisual.getHeight() - enemyHpBar.getHeight() + 6f);
        enemyVisual.addActor(enemyHpBar);
        enemyPanel.add(enemyVisual).size(160, 140).pad(6).padTop(30).padBottom(70);

        conveyor.add(enemyPanel).pad(6);

        enemyReg1Texture = new Texture("art/enemies/reg1.png");
        enemyReg2Texture = new Texture("art/enemies/reg2.png");
        enemyReg3Texture = new Texture("art/enemies/reg3.png");
        enemyBoss1Texture = new Texture("art/enemies/boss1.png");
        enemyBoss2Texture = new Texture("art/enemies/boss2.png");

        // Lower section: fusion station background behind the grid.
        // Scale the frame up uniformly to better align with top section composition.
        gridPanel = new Table();
        gridPanel.setTransform(true);
        // Grid is placed later by Table layout; don't pin it here.
        gridPanel.setPosition(0f, 0f);
        var gridBg = PlaceholderSkinFactory.getDrawableIfPresent(skin, "fusion_station_bg");
        if (gridBg != null) {
            gridPanel.setBackground(gridBg);
            // Keep aspect ratio while scaling.
            gridPanel.setSize(FUSION_FRAME_W, FUSION_FRAME_H);
        }
        Table grid = new Table();
        for (int r = AppConstants.GRID_ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                GridCellWidget cell = new GridCellWidget(skin, ui, c, r);
                cells[c][r] = cell;
                grid.add(cell).size(82, 82).pad(7).padBottom(-2);
            }
            grid.row();
        }

        tube = new TubeWidget(skin, ui);
        cells[AppConstants.TUBE_COL][AppConstants.TUBE_ROW].addActor(tube);
        tube.setPosition(9f, 10f);
        tube.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onTubeTapped != null) onTubeTapped.run();
            }
        });

        // Center the grid inside the fixed frame (slightly down+left for visual balance).
        gridPanel.add(grid).padLeft(22).padRight(36).padTop(4);

        root.add(top).growX().height(60).row();
        root.add(conveyor).growX().expandY().height(300).pad(UiConstants.PAD + 2).padBottom(-18).row();
        // Don't stretch the fusion station frame; center it at native size.
        // Push grid up closer to conveyor.
        root.add(gridPanel).pad(UiConstants.PAD + 2).padTop(-20).center().size(FUSION_FRAME_W, FUSION_FRAME_H);

        // Conveyor belt layer: path anchors + moving sockets.
        beltLayer = new Table();
        beltLayer.setFillParent(true);
        beltLayer.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
        root.addActor(beltLayer);

        // Conveyor loop background image from design export.
        beltLoop = new Table();
        beltLoop.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        beltLoopBaseTexture = new Texture(CONVEYOR_LOOP_BASE_TEXTURE_PATH);
        beltLoopLineTexture = new Texture(CONVEYOR_LOOP_LINE_TEXTURE_PATH);
        beltLoop.setBackground(new TextureRegionDrawable(new TextureRegion(beltLoopBaseTexture)));
        beltLayer.addActor(beltLoop);
        beltLoopLineActor = new MovingBeltLineActor(new TextureRegion(beltLoopLineTexture));
        beltLoopLineActor.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        beltLayer.addActor(beltLoopLineActor);

        pathAnchors = new Table[SOCKET_COUNT];
        for (int i = 0; i < pathAnchors.length; i++) {
            pathAnchors[i] = makePathAnchor();
            beltLayer.addActor(pathAnchors[i]);
        }

        conveyorSockets = new Table[SOCKET_COUNT];
        socketPathIndex = new int[SOCKET_COUNT];
        pathDirectionDegrees = new float[SOCKET_COUNT];
        socketFusion = new FusionInstance[SOCKET_COUNT];
        for (int i = 0; i < SOCKET_COUNT; i++) {
            conveyorSockets[i] = makeSocket(i);
            socketPathIndex[i] = i;
            beltLayer.addActor(conveyorSockets[i]);
        }

        layoutConveyorPath();

        attackZoneMarker = new Table();
        attackZoneMarker.setSize(22, 22);
        root.addActor(attackZoneMarker);

        attackZoneMarkerActor = new AttackZoneMarkerActor(new Color(0.2f, 1f, 0.2f, 0.85f));
        attackZoneMarkerActor.setSize(24, 18);
        root.addActor(attackZoneMarkerActor);
        positionAttackZoneMarker();
    }

    public void update(float delta) {
        beltPhase += delta / Math.max(0.01f, CombatTuning.CONVEYOR_LOOP_SECONDS);
        if (beltPhase >= 1f) beltPhase -= (float) Math.floor(beltPhase);

        // Keep sockets moving even before the first state sync.
        layoutConveyorPath();
        layoutConveyorPathForPhase(beltPhase);
    }

    private Table makePathAnchor() {
        Table t = new Table();
        t.setSize(10, 10);
        t.setVisible(false);
        return t;
    }

    private Table makeSocket(int index) {
        Table t = new Table();
        // Make the drop target larger so it's easy to hit.
        t.setSize(64, 64);
        t.setBackground((Drawable) null);
        t.setVisible(false);
        return t;
    }

    private void layoutConveyorPath() {
        // Fixed 12-point loop in beltLayer coordinates.
        // Keep it stable: do not depend on child actor layout/initialization.
        layoutShaftBackground();
        float margin = 52f;
        // Make the conveyor segment a bit larger.
        float combatTopY = root.getHeight() - 89f;
        float combatBottomY = root.getHeight() - 439f;
        float leftX = margin;
        float rightX = root.getWidth() - margin;

        float topY = combatTopY;
        float bottomY = combatBottomY;

        // Visible belt loop background around the points.
        float loopPad = 50f;
        float rawLoopWidth = (rightX - leftX) + loopPad * 2f;
        float rawLoopHeight = (topY - bottomY) + loopPad * 2f;
        float rawLoopX = leftX - loopPad;
        float rawLoopY = bottomY - loopPad;
        float loopSize = Math.min(rawLoopWidth, rawLoopHeight);
        float loopX = rawLoopX + (rawLoopWidth - loopSize) * 0.5f;
        float loopY = rawLoopY + (rawLoopHeight - loopSize) * 0.5f;
        beltLoop.setSize(loopSize, loopSize);
        beltLoop.setPosition(loopX, loopY);
        beltLoopLineActor.setBounds(loopX, loopY, loopSize, loopSize);

        // Keep sockets on the visual belt track centerline.
        float trackLeft = loopX + loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackRight = loopX + loopSize - loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackOffsetY = loopSize * BELT_TRACK_VERTICAL_OFFSET_RATIO;
        float trackBottom = loopY + loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackTop = loopY + loopSize - loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackCornerRadius = Math.min(trackRight - trackLeft, trackTop - trackBottom) * BELT_TRACK_CORNER_RADIUS_RATIO;
        float perimeter = roundedTrackPerimeter(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius);
        // Sync slot anchors to the animated belt marker phase.
        float slotPhaseOffset = beltLoopLineActor.getSlotCenterOffset(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius);
        for (int i = 0; i < pathAnchors.length; i++) {
            float d = (slotPhaseOffset + perimeter * (i / (float) SOCKET_COUNT)) % perimeter;
            PathSample sample = sampleRoundedTrack(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius, d);
            pathAnchors[i].setPosition(sample.x(), sample.y());
            pathDirectionDegrees[i] = sample.directionDeg();
        }

        // Initial socket placement is handled in syncFromState via continuous sampling.
    }

    private void layoutConveyorPathForPhase(float beltPhase) {
        layoutShaftBackground();
        float margin = 70f;
        float combatTopY = root.getHeight() - 150f;
        float combatBottomY = root.getHeight() - 430f;
        float leftX = margin;
        float rightX = root.getWidth() - margin;

        float topY = combatTopY;
        float bottomY = combatBottomY;

        float loopPad = 50f;
        float rawLoopWidth = (rightX - leftX) + loopPad * 2f;
        float rawLoopHeight = (topY - bottomY) + loopPad * 2f;
        float rawLoopX = leftX - loopPad;
        float rawLoopY = bottomY - loopPad;
        float loopSize = Math.min(rawLoopWidth, rawLoopHeight);
        float loopX = rawLoopX + (rawLoopWidth - loopSize) * 0.5f;
        float loopY = rawLoopY + (rawLoopHeight - loopSize) * 0.5f;

        float trackLeft = loopX + loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackRight = loopX + loopSize - loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackOffsetY = loopSize * BELT_TRACK_VERTICAL_OFFSET_RATIO;
        float trackBottom = loopY + loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackTop = loopY + loopSize - loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackCornerRadius = Math.min(trackRight - trackLeft, trackTop - trackBottom) * BELT_TRACK_CORNER_RADIUS_RATIO;
        float perimeter = roundedTrackPerimeter(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius);
        float startOffset = (float) (trackCornerRadius * Math.PI / 4f);
        float base = (startOffset + ((beltPhase + (0.5f / SOCKET_COUNT)) % 1f) * perimeter) % perimeter;

        for (int socketId = 0; socketId < conveyorSockets.length; socketId++) {
            conveyorSockets[socketId].clearActions();
            int pathIndex = socketPathIndex[socketId];
            float d = (base + perimeter * (pathIndex / (float) SOCKET_COUNT)) % perimeter;
            PathSample sample = sampleRoundedTrack(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius, d);
            float x = sample.x() - conveyorSockets[socketId].getWidth() / 2f;
            float y = sample.y() - conveyorSockets[socketId].getHeight() / 2f;
            conveyorSockets[socketId].setPosition(x, y);
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
            conveyorSockets[socketIndex].addAction(Actions.moveTo(x, y, CombatTuning.CONVEYOR_MOVE_DURATION_SECONDS, Interpolation.smooth));
        }
    }

    private void positionAttackZoneMarker() {
        // Marker stays fixed on the right side of the belt (no phase sync).
        layoutShaftBackground();
        float margin = 70f;
        float combatTopY = root.getHeight() - 150f;
        float combatBottomY = root.getHeight() - 430f;
        float leftX = margin;
        float rightX = root.getWidth() - margin;

        float topY = combatTopY;
        float bottomY = combatBottomY;

        float loopPad = 50f;
        float rawLoopWidth = (rightX - leftX) + loopPad * 2f;
        float rawLoopHeight = (topY - bottomY) + loopPad * 2f;
        float rawLoopX = leftX - loopPad;
        float rawLoopY = bottomY - loopPad;
        float loopSize = Math.min(rawLoopWidth, rawLoopHeight);
        float loopX = rawLoopX + (rawLoopWidth - loopSize) * 0.5f;
        float loopY = rawLoopY + (rawLoopHeight - loopSize) * 0.5f;

        float trackLeft = loopX + loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackRight = loopX + loopSize - loopSize * BELT_TRACK_INSET_X_RATIO;
        float trackOffsetY = loopSize * BELT_TRACK_VERTICAL_OFFSET_RATIO;
        float trackBottom = loopY + loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackTop = loopY + loopSize - loopSize * BELT_TRACK_INSET_Y_RATIO + trackOffsetY;
        float trackCornerRadius = Math.min(trackRight - trackLeft, trackTop - trackBottom) * BELT_TRACK_CORNER_RADIUS_RATIO;

        // Keep marker closer to belt edge.
        // Place marker at the exact combat checkpoint.
        int pathLen = SOCKET_COUNT;
        float idx01 = ((ATTACK_MARKER_PATH_INDEX % pathLen) + pathLen) % pathLen;
        float perimeter = roundedTrackPerimeter(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius);
        // Fixed marker: do NOT sync to belt animation phase.
        float d = (perimeter * (idx01 / (float) pathLen)) % perimeter;
        PathSample sample = sampleRoundedTrack(trackLeft, trackRight, trackBottom, trackTop, trackCornerRadius, d);

        // Nudge outward (to the left) so it hugs the belt edge.
        float x = sample.x() - 18f;
        float y = sample.y();
        Vector2 p = beltLayer.localToStageCoordinates(new Vector2(x, y));
        attackZoneMarker.setVisible(false);
        attackZoneMarkerActor.setPosition(p.x - attackZoneMarkerActor.getWidth() / 2f, p.y - attackZoneMarkerActor.getHeight() / 2f);
    }

    private static final class AttackZoneMarkerActor extends Actor {
        private final ShapeRenderer shapes = new ShapeRenderer();
        private final Color color;

        private AttackZoneMarkerActor(Color color) {
            this.color = new Color(color);
            setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end();
            shapes.setProjectionMatrix(getStage().getCamera().combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(color.r, color.g, color.b, color.a * parentAlpha);

            float x = getX();
            float y = getY();
            float w = getWidth();
            float h = getHeight();
            // Triangle points right toward belt.
            shapes.triangle(x, y, x, y + h, x + w, y + h * 0.5f);
            shapes.end();
            batch.begin();
        }

        public void dispose() {
            shapes.dispose();
        }
    }

    private void layoutShaftBackground() {
        if (shaftBg == null) return;
        float margin = 70f;
        float combatTopY = root.getHeight() - 150f;
        float combatBottomY = root.getHeight() - 430f;
        float leftX = margin;
        float rightX = root.getWidth() - margin;

        float loopPad = 50f;
        float rawLoopWidth = (rightX - leftX) + loopPad * 2f;
        float rawLoopHeight = (combatTopY - combatBottomY) + loopPad * 2f;
        float rawLoopX = leftX - loopPad;
        float rawLoopY = combatBottomY - loopPad;
        float loopSize = Math.min(rawLoopWidth, rawLoopHeight);
        float loopX = rawLoopX + (rawLoopWidth - loopSize) * 0.5f;
        float loopY = rawLoopY + (rawLoopHeight - loopSize) * 0.5f;

        float shaftW = loopSize * 0.432f;
        float shaftH = loopSize * 0.432f;
        float shaftX = loopX + (loopSize - shaftW) * 0.5f;
        float shaftY = loopY + (loopSize - shaftH) * 0.5f;
        shaftBg.setBounds(shaftX, shaftY, shaftW, shaftH);
    }

    private float getPathDirectionDegrees(int pathIndex) {
        if (pathIndex < 0 || pathIndex >= pathDirectionDegrees.length) return 0f;
        return pathDirectionDegrees[pathIndex];
    }

    public Actor getRoot() {
        return root;
    }

    public void dispose() {
        if (attackZoneMarkerActor != null) attackZoneMarkerActor.dispose();
        beltLoopLineTexture.dispose();
        beltLoopBaseTexture.dispose();
        shaftBgTexture.dispose();
        enemyReg1Texture.dispose();
        enemyReg2Texture.dispose();
        enemyReg3Texture.dispose();
        enemyBoss1Texture.dispose();
        enemyBoss2Texture.dispose();
        tube.dispose();

        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                cells[c][r].dispose();
            }
        }
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
        String tubeTxt = "Tube " + state.tubeCharges + "/" + Math.max(1, state.tubeMaxCharges);
        if (state.tubeCooldownRemaining > 0f) tubeTxt += " | CD " + String.format("%.1f", state.tubeCooldownRemaining);
        tubeStatus.setText(tubeTxt + " | HP " + state.tubeHp);

        float tubeCdTotal = state.level == null ? 0f : state.level.tubeCooldownSeconds;
        tube.setCooldown(state.tubeCooldownRemaining, tubeCdTotal);

        float tubePct = state.level == null ? 1f : (state.tubeHp / (float) Math.max(1, state.level.tubeHp));
        tubeHpBar.setPercent(tubePct);

        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                GridCellWidget cell = cells[c][r];
                if (c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW) {
                    continue;
                }
                IngredientInstance inst = state.grid[c][r];
                cell.setIcon(iconFor(inst));
            }
        }

        // Socket visuals show deployed fusions.
        for (int i = 0; i < socketFusion.length; i++) socketFusion[i] = state.conveyorSockets[i];
        for (int i = 0; i < conveyorSockets.length; i++) socketPathIndex[i] = state.conveyorSocketPathIndex[i];

        // Apply socket positions from the state path indices.
        // Positions are sampled continuously along the belt path.
        layoutConveyorPathForPhase(beltPhase);

        for (int i = 0; i < conveyorSockets.length; i++) {
            conveyorSockets[i].clearChildren();
            if (socketFusion[i] == null) {
                conveyorSockets[i].setVisible(true);
                conveyorSockets[i].getColor().a = 0.45f;
            } else {
                conveyorSockets[i].setVisible(true);
                conveyorSockets[i].getColor().a = 1f;
                String iconPath = iconFor(socketFusion[i]);
                if (iconPath != null) {
                    Image icon = new Image(new TextureRegionDrawable(new TextureRegion(new Texture(iconPath))));
                    icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
                    // Make fusion icons 50% bigger on the belt.
                    conveyorSockets[i].add(icon).size(90, 90);
                }
            }
        }

        if (state.activeEnemy == null) {
            enemyHpBar.setPercent(0f);
            enemyVisual.setVisible(false);
        } else {
            enemyIcon.setScale(1f);
            EnemyDefinition def = context.definitions.getEnemy(state.activeEnemy.enemyType).orElse(null);
            int maxHp = def == null ? Math.max(1, state.activeEnemy.hp) : Math.max(1, Math.round(def.maxHp * state.level.enemyHpMultiplier));
            enemyHpBar.setPercent(state.activeEnemy.hp / (float) maxHp);
            enemyVisual.setVisible(true);

            Texture iconTexture = switch (state.activeEnemy.enemyType) {
                case SMUGGLER_GRUNT -> enemyReg1Texture;
                case NET_THROWER -> enemyReg2Texture;
                case TOOL_RAIDER -> enemyReg3Texture;
                case GAS_BOMBER -> enemyReg1Texture;
                case SHIELD_SMUGGLER -> enemyReg2Texture;
                case DRONE_THIEF -> enemyReg3Texture;
                case MUTATION_HUNTER -> enemyReg1Texture;
                case BLACKMARKET_BRUTE -> enemyReg2Texture;
                case BOSS_SMUGGLER_CAPTAIN -> enemyBoss1Texture;
            };
            enemyIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(iconTexture)));
            // Make enemy image 10% smaller.
            enemyIcon.setScale(0.9f);
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

    public Actor getSocketActor(int socketId) {
        if (socketId < 0 || socketId >= conveyorSockets.length) return null;
        return conveyorSockets[socketId];
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

    private static String iconFor(IngredientInstance inst) {
        if (inst == null) return null;
        if (inst instanceof FusionInstance f) {
            if (f.entityType != null && f.itemType != null
                    && f.entityType.name().equalsIgnoreCase("SLIME")
                    && f.itemType.name().equalsIgnoreCase("BATTERY")) {
                return "art/fusions/electroslime.png";
            }
        }
        if (inst instanceof SimpleIngredientInstance s) {
            if (s.kind() == IngredientKind.ENTITY && s.entityType().name().equalsIgnoreCase("SLIME")) {
                return "art/entities/slime.png";
            }
            if (s.kind() == IngredientKind.ITEM && s.itemType().name().equalsIgnoreCase("BATTERY")) {
                return "art/items/battery.png";
            }
        }
        return null;
    }

    private static final class MovingBeltLineActor extends Actor {
        private static final int PIECE_COUNT = SOCKET_COUNT;
        // Increased by 20% to slow the belt down.
        private static final float LOOP_SECONDS = 5.6f * 1.2f;
        private static final float PIECE_SCALE = 0.464f;

        private static final float SLOT_CENTER_OFFSET_FRACTION = 0.5f / PIECE_COUNT;

        private final TextureRegion lineRegion;
        private float progress;

        private MovingBeltLineActor(TextureRegion lineRegion) {
            this.lineRegion = lineRegion;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (LOOP_SECONDS <= 0f) return;
            progress += delta / LOOP_SECONDS;
            if (progress >= 1f) progress -= (float) Math.floor(progress);
        }

        private float getSlotCenterPhase() {
            float p = progress + SLOT_CENTER_OFFSET_FRACTION;
            if (p >= 1f) p -= (float) Math.floor(p);
            return p;
        }

        float getSlotCenterOffset(float left, float right, float bottom, float top, float cornerRadius) {
            float perimeter = roundedTrackPerimeter(left, right, bottom, top, cornerRadius);
            float startOffset = (float) (cornerRadius * Math.PI / 4f);
            return (startOffset + getSlotCenterPhase() * perimeter) % perimeter;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!isVisible() || getWidth() <= 0f || getHeight() <= 0f) return;

            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            float insetX = width * BELT_TRACK_INSET_X_RATIO;
            float insetY = height * BELT_TRACK_INSET_Y_RATIO;
            float offsetY = height * BELT_TRACK_VERTICAL_OFFSET_RATIO;
            float left = x + insetX;
            float right = x + width - insetX;
            float bottom = y + insetY + offsetY;
            float top = y + height - insetY + offsetY;

            float scaleX = width / 317f;
            float scaleY = height / 313f;
            float uniformScale = Math.min(scaleX, scaleY) * PIECE_SCALE;
            float markerW = lineRegion.getRegionWidth() * uniformScale;
            float markerH = lineRegion.getRegionHeight() * uniformScale;

            // Keep piece centerline and corner turns inside the yellow border.
            float sideMargin = markerH * 0.55f;
            left += sideMargin;
            right -= sideMargin;
            bottom += sideMargin;
            top -= sideMargin;
            float cornerRadius = Math.min(right - left, top - bottom) * BELT_TRACK_CORNER_RADIUS_RATIO;
            float perimeter = roundedTrackPerimeter(left, right, bottom, top, cornerRadius);
            float startOffset = (float) (cornerRadius * Math.PI / 4f);

            for (int i = 0; i < PIECE_COUNT; i++) {
                float d = (startOffset + ((progress + (i / (float) PIECE_COUNT)) % 1f) * perimeter) % perimeter;
                PathSample sample = sampleRoundedTrack(left, right, bottom, top, cornerRadius, d);
                float drawX = sample.x() - markerW * 0.5f;
                float drawY = sample.y() - markerH * 0.5f;
                batch.draw(
                        lineRegion,
                        drawX,
                        drawY,
                        markerW * 0.5f,
                        markerH * 0.5f,
                        markerW,
                        markerH,
                        1f,
                        1f,
                        sample.directionDeg()
                );
            }

            batch.setColor(oldR, oldG, oldB, oldA);
        }
    }

    private static float roundedTrackPerimeter(float left, float right, float bottom, float top, float radius) {
        float w = Math.max(1f, right - left);
        float h = Math.max(1f, top - bottom);
        float r = Math.max(1f, Math.min(radius, Math.min(w, h) * 0.49f));
        float horizontal = Math.max(1f, w - 2f * r);
        float vertical = Math.max(1f, h - 2f * r);
        return horizontal * 2f + vertical * 2f + (float) (Math.PI * 2f * r);
    }

    private static PathSample sampleRoundedTrack(float left, float right, float bottom, float top, float radius, float distance) {
        float w = Math.max(1f, right - left);
        float h = Math.max(1f, top - bottom);
        float r = Math.max(1f, Math.min(radius, Math.min(w, h) * 0.49f));
        float horizontal = Math.max(1f, w - 2f * r);
        float vertical = Math.max(1f, h - 2f * r);
        float arc = (float) (Math.PI * 0.5f * r);
        float perimeter = horizontal * 2f + vertical * 2f + arc * 4f;
        float d = ((distance % perimeter) + perimeter) % perimeter;

        float cx;
        float cy;
        float dir;

        if (d < horizontal) {
            cx = left + r + d;
            cy = top;
            dir = 0f;
            return new PathSample(cx, cy, dir);
        }
        d -= horizontal;

        if (d < arc) {
            float a = (float) (Math.PI * 0.5f) - (d / r);
            cx = (right - r) + (float) Math.cos(a) * r;
            cy = (top - r) + (float) Math.sin(a) * r;
            dir = (float) Math.toDegrees(a - Math.PI * 0.5f);
            return new PathSample(cx, cy, dir);
        }
        d -= arc;

        if (d < vertical) {
            cx = right;
            cy = top - r - d;
            dir = -90f;
            return new PathSample(cx, cy, dir);
        }
        d -= vertical;

        if (d < arc) {
            float a = -(d / r);
            cx = (right - r) + (float) Math.cos(a) * r;
            cy = (bottom + r) + (float) Math.sin(a) * r;
            dir = (float) Math.toDegrees(a - Math.PI * 0.5f);
            return new PathSample(cx, cy, dir);
        }
        d -= arc;

        if (d < horizontal) {
            cx = right - r - d;
            cy = bottom;
            dir = 180f;
            return new PathSample(cx, cy, dir);
        }
        d -= horizontal;

        if (d < arc) {
            float a = (float) (-Math.PI * 0.5f - (d / r));
            cx = (left + r) + (float) Math.cos(a) * r;
            cy = (bottom + r) + (float) Math.sin(a) * r;
            dir = (float) Math.toDegrees(a - Math.PI * 0.5f);
            return new PathSample(cx, cy, dir);
        }
        d -= arc;

        if (d < vertical) {
            cx = left;
            cy = bottom + r + d;
            dir = 90f;
            return new PathSample(cx, cy, dir);
        }
        d -= vertical;

        float a = (float) (Math.PI - (d / r));
        cx = (left + r) + (float) Math.cos(a) * r;
        cy = (top - r) + (float) Math.sin(a) * r;
        dir = (float) Math.toDegrees(a - Math.PI * 0.5f);
        return new PathSample(cx, cy, dir);
    }

    private record PathSample(float x, float y, float directionDeg) {
    }

    private static final class SlotGuideActor extends Actor {
        private final TextureRegion lineRegion;

        private SlotGuideActor(TextureRegion lineRegion) {
            this.lineRegion = lineRegion;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color c = getColor();
            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            batch.draw(
                    lineRegion,
                    getX(),
                    getY(),
                    getWidth() / 2f,
                    getHeight() / 2f,
                    getWidth(),
                    getHeight(),
                    1f,
                    1f,
                    getRotation()
            );
            batch.setColor(oldR, oldG, oldB, oldA);
        }
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
            cell.setIconVisible(false);

            // Move only a drag visual; hide source icon so it looks truly moved.
            var iconDrawable = cell.getIconDrawable();
            if (iconDrawable != null) {
                Table dragActor = new Table();
                dragActor.setSize(cell.getWidth(), cell.getHeight());
                Image dragImage = new Image(iconDrawable);
                dragImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);
                float iconSize = 58f;
                dragActor.add(dragImage).size(iconSize, iconSize).center();
                payload.setDragActor(dragActor);

                float iconLeft = (cell.getWidth() - iconSize) * 0.5f;
                float iconBottom = (cell.getHeight() - iconSize) * 0.5f;
                dragAndDrop.setDragActorPosition(x, -y/2);
            } else {
                Table dragActor = new Table();
                dragActor.setSize(cell.getWidth(), cell.getHeight());
                dragActor.setBackground(skin.newDrawable("white", new Color(1f, 1f, 1f, 0.2f)));
                payload.setDragActor(dragActor);
                dragAndDrop.setDragActorPosition(x, -y/2);
            }
            return payload;
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
            cell.setIconVisible(true);
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
            controller.requestDeployFusionToSocket(from.col, from.row, socketIndex);
        }
    }

}
