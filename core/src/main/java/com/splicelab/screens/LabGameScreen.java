package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.combat.CombatController;
import com.splicelab.combat.CombatLog;
import com.splicelab.combat.CombatState;
import com.splicelab.ui.screens.LabGameView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public final class LabGameScreen extends BaseScreen {
    private final int levelNumber;
    private LabGameView view;
    private CombatController combatController;
    private CombatState combatState;

    public LabGameScreen(SpliceLabGame game, GameContext context, int levelNumber) {
        super(game, context);
        this.levelNumber = levelNumber;
    }

    @Override
    protected void buildUi() {
        view = new LabGameView(context);
        stage.addActor(view.getRoot());

        combatController = new CombatController(context);
        combatController.setFeedback(new CombatController.CombatFeedback() {
            @Override
            public int getConveyorPathLength() {
                return view.getConveyorPathLength();
            }

            @Override
            public int mapSlotToPathIndex(boolean leftSide, int slotIndex) {
                // Simple deterministic mapping: use slotIndex, but offset right side.
                int base = Math.max(0, Math.min(slotIndex, view.getConveyorPathLength() - 1));
                return leftSide ? base : (base + 2) % view.getConveyorPathLength();
            }

            @Override
            public void onFusionMoved(boolean leftSide, int slotIndex, int pathIndex) {
                // Sockets are visualized in LabGameView; no slot actor to move here.
            }

            @Override
            public void onFusionAttack(boolean leftSide, int slotIndex, int damage, boolean special) {
                // Projectiles originate from the attacking socket's current position.
                var from = view.getSocketActor(slotIndex);
                var to = view.getEnemyAnchor();
                if (from != null && to != null) view.spawnProjectile(from, to, special ? com.badlogic.gdx.graphics.Color.GOLD : com.badlogic.gdx.graphics.Color.CYAN, null);
                if (special && from != null) view.floatTextNear(from, "SPECIAL!", com.badlogic.gdx.graphics.Color.GOLD);
            }

            @Override
            public void onEnemyDamaged(int damage, boolean special) {
                var anchor = view.getEnemyAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, special ? com.badlogic.gdx.graphics.Color.GOLD : com.badlogic.gdx.graphics.Color.WHITE);
                }
            }

            @Override
            public void onEnemyDefeated() {
                var anchor = view.getEnemyAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "DEFEATED!", com.badlogic.gdx.graphics.Color.ORANGE);
                }
            }

            @Override
            public void onFusionDamaged(boolean leftSide, int slotIndex, int damage) {
                var anchor = view.getSocketActor(slotIndex);
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, com.badlogic.gdx.graphics.Color.SCARLET);
                    view.spawnProjectile(view.getEnemyAnchor(), anchor, com.badlogic.gdx.graphics.Color.GRAY, null);
                }
            }

            @Override
            public void onFusionDestroyed(boolean leftSide, int slotIndex) {
                var anchor = view.getSocketActor(slotIndex);
                if (anchor != null) {
                    view.floatTextNear(anchor, "KO", com.badlogic.gdx.graphics.Color.SCARLET);
                }
            }

            @Override
            public void onTubeDamaged(int damage) {
                var anchor = view.getTubeAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, com.badlogic.gdx.graphics.Color.SCARLET);
                }
            }
        });
        combatState = combatController.startLevel(levelNumber);

        view.setOnTubeTapped(() -> combatController.requestTubeSpawn());
        view.bindDragDrop(combatController);

        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.W) combatController.debugForceWin();
                    if (keycode == Input.Keys.L) combatController.debugForceLose();
                    if (keycode == Input.Keys.S) combatController.requestTubeSpawn();
                    if (keycode == Input.Keys.E) combatController.debugDamageEnemy(9999);
                    return false;
                }
            }));
        }
    }

    @Override
    protected void update(float delta) {
        combatController.update(delta);
        view.syncFromState(combatController.getState());

        switch (combatController.getState().result) {
            case WIN -> {
                int lvl = combatController.getState().level.levelNumber;
                boolean firstWin = context.saves.get().completedLevels.add(lvl);
                CombatLog.d("win reward applied firstWin=" + firstWin);
                context.rewards.apply(combatController.getState().level.rewards, false);
                if (firstWin) {
                    context.rewards.apply(new com.splicelab.model.level.LevelRewardDefinition(
                            combatController.getState().level.rewards.firstWinBonusCoins(),
                            combatController.getState().level.rewards.firstWinBonusDna(),
                            0,
                            0
                    ), false);
                }
                context.saves.get().currentLevel = Math.max(context.saves.get().currentLevel, lvl + 1);
                CombatLog.d("level advanced currentLevel=" + context.saves.get().currentLevel);
                context.saves.save();
                game.setScreen(new LevelCompleteScreen(game, context));
            }
            case LOSE -> game.setScreen(new DefeatScreen(game, context));
            default -> {
            }
        }
    }
}
