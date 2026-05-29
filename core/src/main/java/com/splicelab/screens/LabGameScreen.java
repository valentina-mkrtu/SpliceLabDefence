package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.combat.CombatController;
import com.splicelab.combat.CombatLog;
import com.splicelab.combat.CombatState;
import com.splicelab.ui.screens.LabGameView;
import com.splicelab.ui.windows.ShopDialog;
import com.splicelab.ui.windows.DevPanelDialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.splicelab.debug.DebugFlags;

public final class LabGameScreen extends BaseScreen {
    private final int levelNumber;
    private LabGameView view;
    private CombatController combatController;
    private CombatState combatState;

    private boolean paused;
    private com.badlogic.gdx.scenes.scene2d.ui.Dialog pauseDialog;

    public LabGameScreen(SpliceLabGame game, GameContext context, int levelNumber) {
        super(game, context);
        this.levelNumber = levelNumber;
    }

    @Override
    protected void buildUi() {
        context.audio.startLabLoop();
        context.audio.startBeltLoop();
        view = new LabGameView(context);
        stage.addActor(view.getRoot());

        combatController = new CombatController(context);
        combatController.setFeedback(new CombatController.CombatFeedback() {
            @Override
            public int getConveyorPathLength() {
                return view.getConveyorPathLength();
            }

            // mapSlotToPathIndex() removed — unused (T-5.2)

            @Override
            public void onFusionMoved(boolean leftSide, int slotIndex, int pathIndex) {
                // Sockets are visualized in LabGameView; no slot actor to move here.
            }

            @Override
            public void onFusionAttack(boolean leftSide, int slotIndex, int damage, boolean special) {
                // Projectiles originate from the attacking socket's current position.
                var from = view.getSocketActor(slotIndex);
                var to = view.getEnemyAnchor();
                context.audio.playHeroThrow();
                if (from != null && to != null) view.spawnProjectile(from, to, com.badlogic.gdx.graphics.Color.GREEN, null);
                if (special && from != null) view.floatTextNear(from, "SPECIAL!", com.badlogic.gdx.graphics.Color.GOLD);
            }

            @Override
            public void onEnemyDamaged(int damage, boolean special) {
                var anchor = view.getEnemyAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, special ? com.badlogic.gdx.graphics.Color.GOLD : com.badlogic.gdx.graphics.Color.WHITE);
                }
                view.playHitJuice(special ? 0.85f : 0.45f, special ? 6f : 3.5f);
            }

            @Override
            public void onEnemyDefeated() {
                context.audio.playEnemyDies();
                var anchor = view.getEnemyAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "DEFEATED!", com.badlogic.gdx.graphics.Color.ORANGE);
                }
            }

            @Override
            public void onEnemySpawned() {
                context.audio.playEnemyAppears();
            }

            @Override
            public void onFusionDamaged(boolean leftSide, int slotIndex, int damage) {
                var anchor = view.getSocketActor(slotIndex);
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, com.badlogic.gdx.graphics.Color.SCARLET);
                    view.spawnProjectile(view.getEnemyAnchor(), anchor, com.badlogic.gdx.graphics.Color.RED, null);
                }
                context.audio.playEnemyThrow();
                view.playHitJuice(0.35f, 4.5f);
            }

            @Override
            public void onFusionDestroyed(boolean leftSide, int slotIndex) {
                context.audio.playHeroDies();
                var anchor = view.getSocketActor(slotIndex);
                if (anchor != null) {
                    view.floatTextNear(anchor, "KO", com.badlogic.gdx.graphics.Color.SCARLET);
                }
            }

            @Override
            public void onTubeDamaged(int damage) {
                context.audio.playCrack();
                var anchor = view.getTubeAnchor();
                if (anchor != null) {
                    view.floatTextNear(anchor, "-" + damage, com.badlogic.gdx.graphics.Color.SCARLET);
                    // Enemy throws a red ball at the tube when no fusions are deployed.
                    var from = view.getEnemyAnchor();
                    if (from != null) view.spawnProjectile(from, anchor, com.badlogic.gdx.graphics.Color.RED, null);
                }
                context.audio.playEnemyThrow();
                view.playHitJuice(0.25f, 5.5f);
            }

            @Override
            public void onTimeoutWarning() {
                context.audio.playTimeoutWarning();
            }
        });
        combatState = combatController.startLevel(levelNumber);

        view.setOnTubeTapped(() -> combatController.requestTubeSpawn());
        view.setPauseListener(this::togglePause);
        view.setBoostListeners(
                () -> tryBoost(ShopDialog.PurchaseType.TIME_FREEZE, () -> combatController.activateTimeFreeze(15f)),
                () -> tryBoost(ShopDialog.PurchaseType.IMMEDIATE_COOLDOWN, () -> combatController.activateImmediateCooldown()),
                () -> tryBoost(ShopDialog.PurchaseType.ATK_X2, () -> combatController.activateAtkX2(15f)),
                () -> tryBoost(ShopDialog.PurchaseType.TUBE_HP_RECOVERY, () -> combatController.activateTubeHpRecovery()),
                () -> tryBoost(ShopDialog.PurchaseType.REMOVE_ITEM, () -> combatController.armRemoveOneItem())
        );
        view.refreshBoostCounts(context);
        view.bindDragDrop(combatController);

        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    // Cheat/debug keys are compiled out of release builds via DebugFlags.DEBUG.
                    if (DebugFlags.DEBUG) {
                        if (keycode == Input.Keys.W) combatController.debugForceWin();
                        if (keycode == Input.Keys.L) combatController.debugForceLose();
                        if (keycode == Input.Keys.E) combatController.debugDamageEnemy(9999);
                        if (keycode == Input.Keys.F1) openDevPanel();
                        if (keycode == Input.Keys.NUM_1) tryBoost(ShopDialog.PurchaseType.TIME_FREEZE, () -> combatController.activateTimeFreeze(15f));
                        if (keycode == Input.Keys.NUM_2) tryBoost(ShopDialog.PurchaseType.IMMEDIATE_COOLDOWN, () -> combatController.activateImmediateCooldown());
                        if (keycode == Input.Keys.NUM_3) tryBoost(ShopDialog.PurchaseType.ATK_X2, () -> combatController.activateAtkX2(15f));
                        if (keycode == Input.Keys.NUM_4) tryBoost(ShopDialog.PurchaseType.TUBE_HP_RECOVERY, () -> combatController.activateTubeHpRecovery());
                        if (keycode == Input.Keys.NUM_5) tryBoost(ShopDialog.PurchaseType.REMOVE_ITEM, () -> combatController.armRemoveOneItem());
                    }
                    // Non-cheat desktop shortcuts (always active):
                    if (keycode == Input.Keys.S) combatController.requestTubeSpawn();
                    return false;
                }
            }));
        }
    }

    private boolean tryBoost(ShopDialog.PurchaseType type, Runnable activate) {
        if (type == null || activate == null) return false;
        if (!context.boosts.consume(type.name())) return false;
        activate.run();
        if (view != null) view.refreshBoostCounts(context);
        return true;
    }

    private void togglePause() {
        if (paused) {
            resumeGame();
            return;
        }
        paused = true;
        context.audio.playButtonClick();
        pauseDialog = new com.splicelab.ui.windows.PauseDialog(
                view.getSkin(),
                context,
                this::resumeGame,
                () -> game.setScreen(new MainLobbyScreen(game, context))
        );
        pauseDialog.show(stage);
    }

    private void resumeGame() {
        paused = false;
        if (pauseDialog != null) {
            pauseDialog.hide();
            pauseDialog = null;
        }
    }

    @Override
    protected void openDevPanel() {
        if (!DebugFlags.DEBUG) return;
        new DevPanelDialog(
                view.getSkin(),
                context,
                () -> {
                    if (view != null) view.refreshBoostCounts(context);
                },
                combatController::debugForceWin,
                combatController::debugForceLose
        ).show(stage);
    }

    @Override
    protected void update(float delta) {
        if (paused) return;
        combatController.update(delta);
        view.update(delta);
        view.syncFromState(combatController.getState());

        switch (combatController.getState().result) {
            case WIN -> {
                context.audio.stopBeltLoop();
                context.audio.stopLabLoop();
                context.audio.playWin();
                int lvl = combatController.getState().level.levelNumber;
                boolean firstWin = context.saves.get().completedLevels.add(lvl);
                CombatLog.d("win reward applied firstWin=" + firstWin);
                int dnaEarned = combatController.getState().level.rewards.coins();
                int cryEarned = combatController.getState().level.rewards.dna();
                if (firstWin) {
                    dnaEarned += combatController.getState().level.rewards.firstWinBonusCoins();
                    cryEarned += combatController.getState().level.rewards.firstWinBonusDna();
                }
                context.saves.get().currentLevel = Math.max(context.saves.get().currentLevel, lvl + 1);
                CombatLog.d("level advanced currentLevel=" + context.saves.get().currentLevel);
                context.saves.save();
                game.setScreen(new LevelCompleteScreen(game, context, new com.splicelab.model.level.LevelRewardSummary(dnaEarned, cryEarned)));
            }
            case LOSE -> {
                context.audio.stopBeltLoop();
                context.audio.stopLabLoop();
                context.audio.playLose();
                if (combatController.getState().level != null) {
                    context.telemetry.track(
                            "level_failed",
                            java.util.Map.of(
                                    "level", combatController.getState().level.levelNumber,
                                    "timeDiedSeconds", combatController.getState().endlessMode ? combatController.getState().endlessElapsedSeconds : 0f
                            )
                    );
                }
                // Persist endless survival time for defeat screen.
                context.saves.get().endlessBestSurvivalSeconds = Math.max(
                        context.saves.get().endlessBestSurvivalSeconds,
                        combatController.getState().endlessMode ? combatController.getState().endlessElapsedSeconds : 0f
                );
                context.saves.save();
                context.telemetry.flush();
                game.setScreen(new DefeatScreen(game, context));
            }
            default -> {
            }
        }
    }

    @Override
    public void dispose() {
        if (view != null) view.dispose();
        context.audio.stopBeltLoop();
        context.audio.stopLabLoop();
        super.dispose();
    }
}
