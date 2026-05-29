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
import com.splicelab.debug.DebugFlags;

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
        view.setBoostListeners(
                () -> combatController.activateTimeFreeze(15f),
                () -> combatController.activateImmediateCooldown(),
                () -> combatController.activateAtkX2(15f),
                () -> combatController.activateTubeHpRecovery(),
                () -> combatController.armRemoveOneItem()
        );
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
                        if (keycode == Input.Keys.NUM_1) combatController.activateTimeFreeze(15f);
                        if (keycode == Input.Keys.NUM_2) combatController.activateImmediateCooldown();
                        if (keycode == Input.Keys.NUM_3) combatController.activateAtkX2(15f);
                        if (keycode == Input.Keys.NUM_4) combatController.activateTubeHpRecovery();
                        if (keycode == Input.Keys.NUM_5) combatController.armRemoveOneItem();
                    }
                    // Non-cheat desktop shortcuts (always active):
                    if (keycode == Input.Keys.S) combatController.requestTubeSpawn();
                    return false;
                }
            }));
        }
    }

    @Override
    protected void update(float delta) {
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
                int coinsEarned = combatController.getState().level.rewards.coins();
                int dnaEarned = combatController.getState().level.rewards.dna();
                if (firstWin) {
                    coinsEarned += combatController.getState().level.rewards.firstWinBonusCoins();
                    dnaEarned += combatController.getState().level.rewards.firstWinBonusDna();
                }
                context.saves.get().currentLevel = Math.max(context.saves.get().currentLevel, lvl + 1);
                CombatLog.d("level advanced currentLevel=" + context.saves.get().currentLevel);
                context.saves.save();
                game.setScreen(new LevelCompleteScreen(game, context, new com.splicelab.model.level.LevelRewardSummary(coinsEarned, dnaEarned)));
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
