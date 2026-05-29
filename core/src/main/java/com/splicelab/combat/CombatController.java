package com.splicelab.combat;

import com.splicelab.app.AppConstants;
import com.splicelab.app.GameContext;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.enemy.EnemyDefinition;
import com.splicelab.model.enemy.EnemyInstance;
import com.splicelab.model.enemy.EnemyType;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.ingredient.SimpleIngredientInstance;
import com.splicelab.model.level.LevelDefinition;

import java.util.UUID;

public final class CombatController {
    private final GameContext context;
    private final CombatState state;
    private int instanceCounter;

    // T-4.1: all random calls now route through context.random (the seeded RandomService)
    //         so that DebugFlags.SEEDED_RANDOM actually produces deterministic runs.

    // DEFAULT_TUBE_BAG_SIZE removed — tube bag system deleted (T-5.1)
    private static final int ENDLESS_START_LEVEL = 50;
    private static final float ENDLESS_SCALE_STEP_SECONDS = 60f;
    private static final float ENDLESS_SCALE_STEP_AMOUNT = 0.10f;
    private static final float TIMEOUT_WARNING_SECONDS = 4f;
    // tubeBag field removed — dead legacy spawn system (T-5.1)

    private boolean timeoutWarningFired;

    // Active mid-level buffs applied via applyMidLevelBuff().
    /** Fusion ATK multiplier from mid-level choice (stacks multiplicatively). */
    private float midLevelFusionAtkMult = 1f;
    /** Tube cooldown multiplier from FAST_COOLDOWN buff. */
    private float midLevelTubeCdMult = 1f;
    /** Seconds to add to belt loop when BELT_SPEED is chosen. */
    private float midLevelBeltSpeedBonus = 0f;
    /** Next enemy spawns with reduced attack interval if ENEMY_SLOW chosen. */
    private float midLevelNextEnemyIntervalMult = 1f;

    public interface CombatFeedback {
        int getConveyorPathLength();

        // mapSlotToPathIndex removed — unused (T-5.2)

        void onFusionMoved(boolean leftSide, int slotIndex, int pathIndex);

        void onFusionAttack(boolean leftSide, int slotIndex, int damage, boolean special);

        void onEnemyDamaged(int damage, boolean special);

        void onEnemyDefeated();

        void onEnemySpawned();

        /** Called when an enemy starts charging a telegraphed heavy hit. */
        default void onEnemyTellStart() {}

        /** Called when the telegraphed heavy hit fires. */
        default void onEnemyTellFire(int damage) {}

        /** Called when an enemy enters rage (HP below threshold). */
        default void onEnemyRage() {}

        /** Called when a fusion is stunned by an enemy anti-carry attack. */
        default void onFusionStunned(int socketId, float durationSeconds) {}

        /** Called when the armor of an enemy is broken (depleted to 0). */
        default void onEnemyArmorBroken() {}

        /** Called when the mid-level choice should be shown to the player. */
        default void onMidLevelChoice(MidLevelBuff optionA, MidLevelBuff optionB) {}

        void onFusionDamaged(boolean leftSide, int slotIndex, int damage);

        void onFusionDestroyed(boolean leftSide, int slotIndex);

        void onTubeDamaged(int damage);

        void onTimeoutWarning();
    }

    private CombatFeedback feedback;

    public CombatController(GameContext context) {
        this.context = context;
        this.state = new CombatState(context.config.maxConveyorSlotsPerSide);
    }

    public void setFeedback(CombatFeedback feedback) {
        this.feedback = feedback;
    }

    public CombatState startLevel(int levelNumber) {
        boolean endless = levelNumber > ENDLESS_START_LEVEL;
        int loadLevelNumber = endless ? ENDLESS_START_LEVEL : levelNumber;
        LevelDefinition level = context.levels.getLevel(loadLevelNumber).orElse(null);
        if (level == null) {
            CombatLog.d("Missing level " + loadLevelNumber);
            state.result = CombatResult.LOSE;
            return state;
        }

        clearGrid();
        clearConveyor();

        state.levelNumber = levelNumber;
        state.level = level;
        state.remainingTimeSeconds = level.durationSeconds;
        timeoutWarningFired = false;
        state.tubeHp = level.tubeHp > 0 ? level.tubeHp : context.config.tubeMaxHp;

        float cd = level.tubeCooldownSeconds <= 0f ? context.config.tubeCooldownSeconds : level.tubeCooldownSeconds;
        int charges = level.maxTubeCharges <= 0 ? context.config.maxTubeCharges : level.maxTubeCharges;
        charges = Math.max(1, charges);
        cd = Math.max(0.25f, cd);

        state.tubeCooldownRemaining = 0f;
        state.tubeMaxCharges = charges;
        state.tubeCharges = charges;
        state.consecutiveItemSpawns = 0;
        state.activeEnemy = null;
        state.enemySpawnCooldownRemaining = 0f;
        state.enemyWaveIndex = 0;
        // T-4.3: initial cooldown is set by spawnEnemyOfType() from the enemy definition.
        // Reset to 0 so the first enemy spawns and sets its own interval immediately.
        state.enemyAttackCooldownRemaining = 0f;

        state.endlessMode = endless;
        state.endlessElapsedSeconds = 0f;
        state.endlessScalingStepsApplied = 0;
        state.endlessHpMultiplierBonus = 0f;
        state.endlessAtkMultiplierBonus = 0f;
        state.conveyorStepCooldownRemaining = CombatTuning.CONVEYOR_STEP_INTERVAL_SECONDS;
        for (int i = 0; i < state.fusionAttackCooldownSockets.length; i++) state.fusionAttackCooldownSockets[i] = 0f;

        state.result = CombatResult.RUNNING;
        state.midLevelChoiceOffered = false;
        state.midLevelChoicePending = false;

        // Reset per-level mid-level buff state.
        midLevelFusionAtkMult = 1f;
        midLevelTubeCdMult = 1f;
        midLevelBeltSpeedBonus = 0f;
        midLevelNextEnemyIntervalMult = 1f;

        context.saves.get().unlockedConveyorSlotsLeft = Math.max(context.saves.get().unlockedConveyorSlotsLeft, level.unlockedConveyorSlotsLeft);
        context.saves.get().unlockedConveyorSlotsRight = Math.max(context.saves.get().unlockedConveyorSlotsRight, level.unlockedConveyorSlotsRight);
        context.saves.save();

        CombatLog.d("LEVEL_START level=" + level.levelNumber);
        if (endless) CombatLog.d("ENDLESS_MODE enabled using level=" + ENDLESS_START_LEVEL);
        CombatLog.d("allowedEntities=" + level.availableEntities);
        CombatLog.d("allowedItems=" + level.availableItems);
        CombatLog.d("enemyPool=" + level.enemyPool);
        CombatLog.d("tubeHp=" + level.tubeHp + " durationSeconds=" + level.durationSeconds);
        CombatLog.d("rewards coins=" + level.rewards.coins() + " dna=" + level.rewards.dna());
        CombatLog.d("tubeCooldownSeconds=" + cd + " maxTubeCharges=" + charges);

        if (state.activeEnemy == null) {
            ensureEnemySpawned();
        }
        return state;
    }

    public CombatState getState() {
        return state;
    }

    public boolean activateTimeFreeze(float seconds) {
        if (state.result != CombatResult.RUNNING) return false;
        state.timeFreezeSecondsRemaining = Math.max(state.timeFreezeSecondsRemaining, Math.max(0f, seconds));
        return true;
    }

    public boolean activateAtkX2(float seconds) {
        if (state.result != CombatResult.RUNNING) return false;
        state.atkX2SecondsRemaining = Math.max(state.atkX2SecondsRemaining, Math.max(0f, seconds));
        return true;
    }

    public boolean activateImmediateCooldown() {
        if (state.result != CombatResult.RUNNING) return false;
        state.tubeCooldownRemaining = 0f;
        if (state.tubeCharges <= 0) {
            state.tubeCharges = state.tubeMaxCharges;
            }
        // Safety: allow an immediate spit even if the UI/state got out of sync.
        if (state.tubeCharges <= 0) state.tubeCharges = Math.max(1, state.tubeMaxCharges);
        return true;
    }

    public boolean activateTubeHpRecovery() {
        if (state.result != CombatResult.RUNNING) return false;
        int maxHp = state.level != null && state.level.tubeHp > 0 ? state.level.tubeHp : context.config.tubeMaxHp;
        state.tubeHp = Math.max(1, maxHp);
        return true;
    }

    public boolean armRemoveOneItem() {
        if (state.result != CombatResult.RUNNING) return false;
        state.removeItemArmed = true;
        return true;
    }

    public void update(float delta) {
        if (state.result != CombatResult.RUNNING) return;

        // Boost timers.
        if (state.timeFreezeSecondsRemaining > 0f) {
            state.timeFreezeSecondsRemaining = Math.max(0f, state.timeFreezeSecondsRemaining - Math.max(0f, delta));
        }
        if (state.atkX2SecondsRemaining > 0f) {
            state.atkX2SecondsRemaining = Math.max(0f, state.atkX2SecondsRemaining - Math.max(0f, delta));
        }

        if (state.endlessMode) {
            state.endlessElapsedSeconds += Math.max(0f, delta);
            int steps = (int) Math.floor(state.endlessElapsedSeconds / ENDLESS_SCALE_STEP_SECONDS);
            if (steps > state.endlessScalingStepsApplied) {
                int add = steps - state.endlessScalingStepsApplied;
                state.endlessScalingStepsApplied = steps;
                state.endlessHpMultiplierBonus += ENDLESS_SCALE_STEP_AMOUNT * add;
                state.endlessAtkMultiplierBonus += ENDLESS_SCALE_STEP_AMOUNT * add;
                CombatLog.d("ENDLESS_SCALE steps=" + steps + " hpBonus=" + state.endlessHpMultiplierBonus + " atkBonus=" + state.endlessAtkMultiplierBonus);
            }
        }

        boolean frozen = state.timeFreezeSecondsRemaining > 0f;

        if (state.remainingTimeSeconds > 0f && !frozen) {
            state.remainingTimeSeconds = Math.max(0f, state.remainingTimeSeconds - delta * (com.splicelab.debug.DebugFlags.FAST_ROUND_TIMER ? 3f : 1f));

            if (!timeoutWarningFired && state.remainingTimeSeconds > 0f && state.remainingTimeSeconds <= TIMEOUT_WARNING_SECONDS) {
                timeoutWarningFired = true;
                if (feedback != null) feedback.onTimeoutWarning();
            }

            if (state.remainingTimeSeconds <= 0f) {
                state.result = state.endlessMode ? CombatResult.LOSE : CombatResult.WIN;
                return;
            }
        }

        if (state.tubeCooldownRemaining > 0f && !frozen) {
            state.tubeCooldownRemaining = Math.max(0f, state.tubeCooldownRemaining - delta);
        }

        // Tube cooldown refill: cooldown only happens when empty.
        if (state.level != null && state.tubeCharges <= 0) {
            if (state.tubeCooldownRemaining <= 0f) {
                state.tubeCharges = state.tubeMaxCharges;
                    }
        }

        if (state.enemySpawnCooldownRemaining > 0f && !frozen) {
            state.enemySpawnCooldownRemaining = Math.max(0f, state.enemySpawnCooldownRemaining - delta);
        }

        ensureEnemySpawned();

        if (!frozen) updateConveyorPhase(delta);
        // Conveyor sockets are visual belt pockets; they don't advance occupancy.

        // Mid-level choice: offer once at the halfway mark.
        if (!state.midLevelChoiceOffered && !frozen && state.level != null) {
            float halfTime = state.level.durationSeconds * CombatTuning.MID_LEVEL_CHOICE_FRACTION;
            float elapsed = state.level.durationSeconds - state.remainingTimeSeconds;
            if (elapsed >= halfTime) {
                state.midLevelChoiceOffered = true;
                state.midLevelChoicePending = true;
                MidLevelBuff[] pool = MidLevelBuff.values();
                MidLevelBuff a = pool[context.random.nextInt(pool.length)];
                MidLevelBuff b;
                do { b = pool[context.random.nextInt(pool.length)]; } while (b == a);
                if (feedback != null) feedback.onMidLevelChoice(a, b);
            }
        }

        if (!frozen) {
            updateFusionAutoAttack(delta);
            updateEnemyAttack(delta);
            updateEnemyTell(delta);
            tickStun(delta);
        }
    }

    private void updateConveyorPhase(float delta) {
        float loopSeconds = Math.max(0.01f, getBeltLoopSeconds());
        state.conveyorBeltPhase += delta / loopSeconds;
        if (state.conveyorBeltPhase >= 1f) state.conveyorBeltPhase -= (float) Math.floor(state.conveyorBeltPhase);
    }

    // updateConveyorMovement(float) removed — empty stub (T-5.2)

    public CommandResult requestTubeSpawn() {
        if (state.result != CombatResult.RUNNING) {
            return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        }

        // Prevent over-spawning (double taps / duplicated UI events).
        // Charges are the authoritative cap per round.
        if (state.tubeCharges <= 0) {
            if (state.tubeCooldownRemaining <= 0f) state.tubeCooldownRemaining = getTubeCooldownSeconds();
            return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "No tube charges");
        }

        if (!com.splicelab.debug.DebugFlags.FREE_TUBE_SPAWN && state.tubeCooldownRemaining > 0f) {
            return CommandResult.fail(CommandResult.Code.TUBE_ON_COOLDOWN, "Tube on cooldown");
        }

        // T-5.3: build the empty-cell list once; pass it to both the crowd-check and placement.
        java.util.List<int[]> emptyCells = collectEmptyNonTubeCells();
        if (emptyCells.isEmpty()) {
            return CommandResult.fail(CommandResult.Code.NO_EMPTY_GRID_CELL, "No empty grid cell");
        }
        int[] empty = emptyCells.get(context.random.nextInt(emptyCells.size()));

        var choice = chooseTubeSpawnWithPity(emptyCells.size());
        if (choice == null || choice.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.NONE) {
            return CommandResult.fail(CommandResult.Code.INVALID_LEVEL, "No spawn choices");
        }

        String id = nextInstanceId();
        IngredientInstance instance;
        if (choice.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.ENTITY) {
            EntityType e = choice.entityType();
            instance = SimpleIngredientInstance.ofEntity(id, e);
            state.consecutiveItemSpawns = 0;
        } else {
            ItemType i = choice.itemType();
            instance = SimpleIngredientInstance.ofItem(id, i);
            state.consecutiveItemSpawns++;
        }

        state.grid[empty[0]][empty[1]] = instance;
        state.tubeCharges = Math.max(0, state.tubeCharges - 1);

        // Cooldown only after the last spit.
        if (state.tubeCharges <= 0) state.tubeCooldownRemaining = getTubeCooldownSeconds();

        CombatLog.d("spawn ingredient type=" + choice.type() + " at=" + empty[0] + "," + empty[1]);
        return CommandResult.ok();
    }

    public CommandResult requestRemoveAtCell(int col, int row) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!state.removeItemArmed) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Remove not armed");
        if (!isValidCell(col, row) || isTubeCell(col, row)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");

        IngredientInstance target = state.grid[col][row];
        if (target == null) return CommandResult.fail(CommandResult.Code.CELL_EMPTY, "Cell empty");
        state.grid[col][row] = null;
        state.removeItemArmed = false;
        return CommandResult.ok();
    }

    /** T-5.3: accepts pre-computed empty cell count so the caller doesn't scan twice. */
    private com.splicelab.services.TubeSpawnService.SpawnChoice chooseTubeSpawnWithPity(int emptyCount) {
        // Grid safety: if grid is getting full, bias toward entities.
        // Items without entities create deadlocks where the player can't fuse.
        boolean gridCrowded = emptyCount <= 2;

        int pityEveryX = Math.max(0, context.config.pityGuaranteeEntityEveryXItemSpawns);
        boolean forceEntity = pityEveryX > 0 && state.consecutiveItemSpawns >= pityEveryX;

        float wEntity = Math.max(0f, context.config.spawnEntityWeight);
        float wItem = Math.max(0f, context.config.spawnItemWeight);
        boolean wantEntity;
        if (gridCrowded) {
            wantEntity = true;
        } else if (forceEntity) {
            wantEntity = true;
        } else {
            float total = wEntity + wItem;
            if (total <= 0f) {
                wantEntity = context.random.nextFloat() < 0.5f;
            } else {
                wantEntity = context.random.nextFloat() * total < wEntity;
            }
        }

        // Pull from the level's unlocked pools (not the old 50/50 bag).
        var picked = context.tubeSpawnService.chooseSpawnForLevel(state.level.levelNumber);
        if (picked.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.NONE) return picked;

        // If picked type doesn't match desired, try a few rerolls.
        int maxRerolls = gridCrowded ? 12 : 6;
        for (int tries = 0; tries < maxRerolls; tries++) {
            boolean isEntity = picked.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.ENTITY;
            if (wantEntity == isEntity) return picked;
            picked = context.tubeSpawnService.chooseSpawnForLevel(state.level.levelNumber);
            if (picked.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.NONE) return picked;
        }

        // Fallback: return whatever we got.
        return picked;
    }

    private float getTubeCooldownSeconds() {
        float cd = state.level == null ? context.config.tubeCooldownSeconds : state.level.tubeCooldownSeconds;
        if (cd <= 0f) cd = context.config.tubeCooldownSeconds;
        cd *= midLevelTubeCdMult; // FAST_COOLDOWN buff halves this
        return Math.max(0.25f, cd);
    }

    // refillTubeBagIfNeeded() removed — dead legacy system (T-5.1)

    public CommandResult requestMoveIngredient(int fromCol, int fromRow, int toCol, int toRow) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(fromCol, fromRow) || !isValidCell(toCol, toRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");
        if (isTubeCell(toCol, toRow) || isTubeCell(fromCol, fromRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Tube cell");

        IngredientInstance src = state.grid[fromCol][fromRow];
        if (src == null) return CommandResult.fail(CommandResult.Code.CELL_EMPTY, "Source empty");
        if (state.grid[toCol][toRow] != null) return CommandResult.fail(CommandResult.Code.CELL_OCCUPIED, "Target occupied");

        state.grid[toCol][toRow] = src;
        state.grid[fromCol][fromRow] = null;
        return CommandResult.ok();
    }

    public CommandResult requestMoveOrFuse(int fromCol, int fromRow, int toCol, int toRow) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(fromCol, fromRow) || !isValidCell(toCol, toRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");
        if (isTubeCell(toCol, toRow) || isTubeCell(fromCol, fromRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Tube cell");

        if (fromCol == toCol && fromRow == toRow) return CommandResult.ok();

        IngredientInstance src = state.grid[fromCol][fromRow];
        if (src == null) return CommandResult.fail(CommandResult.Code.CELL_EMPTY, "Source empty");

        IngredientInstance dst = state.grid[toCol][toRow];
        if (dst == null) {
            return requestMoveIngredient(fromCol, fromRow, toCol, toRow);
        }

        CommandResult fuse = requestFuse(fromCol, fromRow, toCol, toRow);
        if (fuse.success) {
            CombatLog.d("fusion created at=" + toCol + "," + toRow);
            return fuse;
        }
        return fuse;
    }

    public CommandResult requestFuse(int colA, int rowA, int colB, int rowB) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(colA, rowA) || !isValidCell(colB, rowB)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");
        if (isTubeCell(colA, rowA) || isTubeCell(colB, rowB)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Tube cell");

        IngredientInstance a = state.grid[colA][rowA];
        IngredientInstance b = state.grid[colB][rowB];
        if (a == null || b == null) return CommandResult.fail(CommandResult.Code.CELL_EMPTY, "Missing ingredient");

        SimpleIngredientInstance entity;
        SimpleIngredientInstance item;
        if (a instanceof SimpleIngredientInstance sa && sa.kind() == com.splicelab.model.IngredientKind.ENTITY && b instanceof SimpleIngredientInstance sb && sb.kind() == com.splicelab.model.IngredientKind.ITEM) {
            entity = sa;
            item = sb;
        } else if (b instanceof SimpleIngredientInstance sb && sb.kind() == com.splicelab.model.IngredientKind.ENTITY && a instanceof SimpleIngredientInstance sa && sa.kind() == com.splicelab.model.IngredientKind.ITEM) {
            entity = sb;
            item = sa;
        } else {
            return CommandResult.fail(CommandResult.Code.INVALID_FUSION, "Need entity + item");
        }

        if (!context.fusionService.canFuse(entity.entityType(), item.itemType())) {
            return CommandResult.fail(CommandResult.Code.INVALID_FUSION, "No fusion for pair");
        }

        FusionInstance fusion = context.fusionService.createFusion(nextInstanceId(), entity.entityType(), item.itemType(), state.levelNumber).orElse(null);
        if (fusion == null) return CommandResult.fail(CommandResult.Code.INVALID_FUSION, "Fusion creation failed");

        context.fusionUnlocks.unlock(entity.entityType().name() + "+" + item.itemType().name());

        state.grid[colB][rowB] = fusion;
        state.grid[colA][rowA] = null;
        return CommandResult.ok();
    }

    // requestDeployFusionFromGrid() removed — deprecated stub replaced by socket deployment (T-5.2)

    public CommandResult requestDeployFusionToSocket(int fromCol, int fromRow, int socketId) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(fromCol, fromRow) || isTubeCell(fromCol, fromRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");
        if (socketId < 0 || socketId >= state.conveyorSockets.length) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Bad socket");
        if (state.conveyorSockets[socketId] != null) return CommandResult.fail(CommandResult.Code.SLOT_OCCUPIED, "Socket occupied");

        IngredientInstance src = state.grid[fromCol][fromRow];
        if (!(src instanceof FusionInstance fusion)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Not a fusion");

        state.conveyorSockets[socketId] = fusion;
        state.grid[fromCol][fromRow] = null;
        state.fusionAttackCooldownSockets[socketId] = 0f;

        context.telemetry.track(
                "fusion_deployed",
                java.util.Map.of("fusionId", fusion.entityType.name() + "_" + fusion.itemType.name())
        );

        CombatLog.d("fusion deployed socket=" + socketId);
        return CommandResult.ok();
    }

    public CommandResult requestDeployFusionFromGridToSocket(int fromCol, int fromRow, int socketId) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(fromCol, fromRow) || isTubeCell(fromCol, fromRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");
        if (socketId < 0 || socketId >= state.conveyorSockets.length) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Bad socket");

        IngredientInstance src = state.grid[fromCol][fromRow];
        if (!(src instanceof FusionInstance fusion)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Not a fusion");
        if (state.conveyorSockets[socketId] != null) return CommandResult.fail(CommandResult.Code.SLOT_OCCUPIED, "Socket occupied");

        state.conveyorSockets[socketId] = fusion;
        state.grid[fromCol][fromRow] = null;

        context.telemetry.track(
                "fusion_deployed",
                java.util.Map.of("fusionId", fusion.entityType.name() + "_" + fusion.itemType.name())
        );

        return CommandResult.ok();
    }

    private boolean isPathIndexOccupied(int idx, int pathLen) {
        if (idx < 0 || idx >= pathLen) return false;
        for (int i = 0; i < state.conveyorSockets.length; i++) {
            if (state.conveyorSockets[i] != null && state.conveyorSocketPathIndex[i] == idx) return true;
        }
        return false;
    }

    public void debugForceWin() {
        if (state.result == CombatResult.RUNNING) state.result = CombatResult.WIN;
    }

    public void debugForceLose() {
        if (state.result == CombatResult.RUNNING) state.result = CombatResult.LOSE;
    }

    public void debugDamageEnemy(int amount) {
        if (state.activeEnemy == null) return;
        // Strip armor first, then damage HP.
        int dmg = Math.max(1, amount);
        if (state.activeEnemy.armor > 0) {
            int abs = Math.min(state.activeEnemy.armor, dmg);
            state.activeEnemy.armor -= abs;
            dmg -= abs;
        }
        if (dmg > 0) state.activeEnemy.hp = Math.max(0, state.activeEnemy.hp - dmg);
        if (state.activeEnemy.hp <= 0) {
            CombatLog.d("enemy defeated type=" + state.activeEnemy.enemyType);
            if (feedback != null) feedback.onEnemyDefeated();
            state.activeEnemy = null;
            state.enemySpawnCooldownRemaining = getDynamicSpawnIntervalSeconds();
        }
    }

    private boolean isTubeCell(int c, int r) {
        return c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW;
    }

    private boolean isValidCell(int c, int r) {
        return c >= 0 && c < AppConstants.GRID_COLS && r >= 0 && r < AppConstants.GRID_ROWS;
    }

    /**
     * T-5.3: Returns all empty non-tube cells in a single pass.
     * Callers use the list for both the crowd-check count and random placement pick,
     * eliminating the previous double-scan.
     */
    private java.util.List<int[]> collectEmptyNonTubeCells() {
        java.util.List<int[]> cells = new java.util.ArrayList<>();
        for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                if (isTubeCell(c, r)) continue;
                if (state.grid[c][r] == null) cells.add(new int[]{c, r});
            }
        }
        return cells;
    }

    private void ensureEnemySpawned() {
        if (state.level == null) return;
        if (state.activeEnemy != null) return;
        if (state.enemySpawnCooldownRemaining > 0f) return;
        if (state.level.enemyWave != null && !state.level.enemyWave.isEmpty()) {
            EnemyType chosenType = state.level.enemyWave.get(state.enemyWaveIndex % state.level.enemyWave.size());
            state.enemyWaveIndex++;
            spawnEnemyOfType(chosenType);
            return;
        }
        if (state.level.enemyPool.isEmpty()) return;

        EnemyType chosenType = chooseWeightedEnemyType(state.level);
        spawnEnemyOfType(chosenType);
    }

    private void spawnEnemyOfType(EnemyType chosenType) {
        EnemyDefinition def = context.definitions.getEnemy(chosenType).orElse(null);
        if (def == null) return;

        float dynamicMult = computeDynamicEnemyMultiplier();
        float bossMult = chosenType == EnemyType.BOSS_SMUGGLER_CAPTAIN ? CombatTuning.BOSS_BASE_HP_MULT : 1f;
        int scaledHp = Math.max(1, Math.round(def.maxHp * state.level.enemyHpMultiplier * dynamicMult * bossMult));

        state.activeEnemy = new EnemyInstance(nextInstanceId(), chosenType, scaledHp);

        // Assign armor based on enemy tier.
        state.activeEnemy.armor = armorForType(chosenType);

        // Apply ENEMY_SLOW buff from previous mid-level choice.
        float interval = def.attack.intervalSeconds() * midLevelNextEnemyIntervalMult;
        midLevelNextEnemyIntervalMult = 1f; // consume it
        state.enemyAttackCooldownRemaining = interval;

        CombatLog.d("enemy spawned type=" + chosenType + " hp=" + scaledHp + " armor=" + state.activeEnemy.armor);
        if (feedback != null) feedback.onEnemySpawned();
    }

    private static int armorForType(EnemyType type) {
        return switch (type) {
            case BOSS_SMUGGLER_CAPTAIN -> CombatTuning.ARMOR_BOSS;
            case SHIELD_SMUGGLER, DRONE_THIEF, MUTATION_HUNTER, BLACKMARKET_BRUTE -> CombatTuning.ARMOR_TOUGH_ENEMY;
            default -> CombatTuning.ARMOR_REGULAR_ENEMY;
        };
    }

    private float computeDynamicEnemyMultiplier() {
        // T-33: scale from player belt DPS instead of raw fusion count.
        float beltDps = computeBeltTotalDps();
        float dpsExtra = Math.max(0f, (beltDps - CombatTuning.DPS_SCALE_BASE_DPS) / 100f);
        float dpsMult = 1f + Math.min(CombatTuning.DPS_SCALE_MAX_EXTRA, dpsExtra * CombatTuning.DPS_SCALE_PER_100_DPS);

        int deployedFusions = 0;
        for (FusionInstance f : state.conveyorSockets) {
            if (f != null && f.hp > 0) deployedFusions++;
        }

        int extra = Math.max(0, deployedFusions - 1);
        float tier = getDifficultyTierFactor();
        float mult = 1f + (CombatTuning.DYNAMIC_DIFFICULTY_PER_FUSION_EXTRA_HP * tier) * extra;
        mult *= computeFusionSpikeMultiplier(deployedFusions);
        mult *= dpsMult;
        if (state.endlessMode) mult *= (1f + Math.max(0f, state.endlessHpMultiplierBonus));
        return mult;
    }

    private float computeFusionSpikeMultiplier(int deployedFusions) {
        if (deployedFusions < CombatTuning.FUSION_SPIKE_START) return 1f;
        int extraPairs = Math.max(0, (deployedFusions - CombatTuning.FUSION_SPIKE_START) / 2);
        return CombatTuning.FUSION_SPIKE_BASE_MULT + CombatTuning.FUSION_SPIKE_PER_2_FUSIONS_EXTRA * extraPairs;
    }

    private float getDifficultyTierFactor() {
        // T-5.4: literals replaced with CombatTuning constants.
        int level = Math.max(1, state.levelNumber);
        if (level <= CombatTuning.TIER_GENTLE_CAP) return CombatTuning.TIER_GENTLE_FACTOR;

        int bumps = 1 + Math.max(0, (level - CombatTuning.TIER_RAMP_START) / CombatTuning.TIER_BUMP_INTERVAL);
        float withinBand = ((level - CombatTuning.TIER_RAMP_START) % CombatTuning.TIER_BUMP_INTERVAL) / 4f;
        float tier = CombatTuning.TIER_RAMP_BASE
                + CombatTuning.TIER_BUMP_FACTOR * bumps
                + CombatTuning.TIER_INTRA_BAND_FACTOR * withinBand;
        return Math.max(CombatTuning.TIER_MIN, Math.min(CombatTuning.TIER_MAX, tier));
    }

    private EnemyType chooseWeightedEnemyType(LevelDefinition level) {
        float total = 0f;
        for (var e : level.enemyPool) total += e.weight() > 0f ? e.weight() : 0f;
        if (total <= 0f) {
            return level.enemyPool.get(context.random.nextInt(level.enemyPool.size())).enemyType();
        }
        float r = context.random.nextFloat() * total;
        float acc = 0f;
        for (var e : level.enemyPool) {
            float w = e.weight() > 0f ? e.weight() : 0f;
            acc += w;
            if (r <= acc) return e.enemyType();
        }
        return level.enemyPool.get(level.enemyPool.size() - 1).enemyType();
    }

    private void updateFusionAutoAttack(float delta) {
        if (state.activeEnemy == null) return;

        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance fusion = state.conveyorSockets[socketId];
            if (fusion == null) continue;

            // Anti-carry stun: skip attack if this socket is stunned.
            if (state.activeEnemy != null
                    && state.activeEnemy.stunnedSocketId == socketId
                    && state.activeEnemy.stunRemaining > 0f) {
                continue;
            }

            state.fusionAttackCooldownSockets[socketId] = Math.max(0f, state.fusionAttackCooldownSockets[socketId] - delta);

            // Gameplay: each fusion has an attack interval.
            // Visual: only fire when the belt reaches the attack checkpoint.
            if (state.fusionAttackCooldownSockets[socketId] > 0f) continue;
            if (!isSocketAtAttackCheckpoint(socketId)) continue;

            attackEnemyFromFusionSocket(socketId, fusion);

            float interval = fusion.stats == null ? 1f : fusion.stats.attackIntervalSeconds();
            interval = Math.max(CombatTuning.MIN_ATTACK_INTERVAL_SECONDS, interval);
            // Slight jitter so stacks feel less robotic.
            interval *= context.random.range(0.90f, 1.10f);
            state.fusionAttackCooldownSockets[socketId] = interval;
        }
    }

    private boolean isSocketAtAttackCheckpoint(int socketId) {
        int pathLen = CombatTuning.ATTACK_ZONE_INDEX <= 0 ? 0 : (feedback == null ? 0 : feedback.getConveyorPathLength());
        if (pathLen <= 0) return false;

        // Match LabGameView.layoutConveyorPathForPhase(): it offsets phase by half a slot.
        float phase = state.conveyorBeltPhase;
        float shifted = (phase + (0.5f / pathLen)) % 1f;
        float idxF = (shifted * pathLen) % pathLen;
        int beltIndex = ((int) Math.floor(idxF)) % pathLen;

        int socketIndex = ((socketId % pathLen) + pathLen) % pathLen;
        int checkpointIndex = ((CombatTuning.ATTACK_ZONE_INDEX % pathLen) + pathLen) % pathLen;
        int current = (socketIndex + beltIndex) % pathLen;

        if (current != checkpointIndex) return false;

        // Window so we don't skip it between frames.
        float frac = idxF - (float) Math.floor(idxF);
        return frac < CombatTuning.FUSION_LOW_HEALTH_THRESHOLD;
    }

    private void attackEnemyFromFusionSocket(int socketId, FusionInstance fusion) {
        if (state.activeEnemy == null) return;

        if (!isSocketAtAttackCheckpoint(socketId)) return;
        CombatLog.d("fusion at attack zone socket=" + socketId);

        int base = Math.max(0, fusion.stats.atk());
        float variance = Math.max(0f, fusion.stats.variance());
        float roll = context.random.range(-variance, variance);
        float atkMult = state.atkX2SecondsRemaining > 0f ? 2f : 1f;
        atkMult *= midLevelFusionAtkMult; // mid-level buff
        int dmg = Math.max(CombatTuning.MIN_DAMAGE, Math.round(base * atkMult * (1f + roll)));

        boolean special = context.random.chance(fusion.stats.specialChance());
        if (special) dmg *= 2;

        CombatLog.d("fusion hit enemy dmg=" + dmg + (special ? " SPECIAL" : ""));

        if (feedback != null) {
            feedback.onFusionAttack(true, socketId, dmg, special);
            feedback.onEnemyDamaged(dmg, special);
        }

        // Armor absorbs damage before HP.
        int remaining = dmg;
        if (state.activeEnemy.armor > 0) {
            int absorbed = Math.min(state.activeEnemy.armor, remaining);
            state.activeEnemy.armor -= absorbed;
            remaining -= absorbed;
            if (state.activeEnemy.armor == 0) {
                CombatLog.d("enemy armor broken by fusion hit");
                if (feedback != null) feedback.onEnemyArmorBroken();
            }
        }

        if (remaining > 0) {
            state.activeEnemy.hp = Math.max(0, state.activeEnemy.hp - remaining);
        }

        if (state.activeEnemy.hp <= 0) {
            CombatLog.d("enemy defeated type=" + state.activeEnemy.enemyType);
            if (feedback != null) feedback.onEnemyDefeated();
            state.activeEnemy = null;
            state.enemySpawnCooldownRemaining = getDynamicSpawnIntervalSeconds() + CombatTuning.ENEMY_SPAWN_DELAY_AFTER_DEATH_SECONDS;
        }
    }

    private void updateEnemyAttack(float delta) {
        if (state.level == null) return;
        if (state.activeEnemy == null) return;
        // Don't start a new attack while a tell is charging.
        if (state.activeEnemy.chargingTell > 0f) return;

        EnemyDefinition def = context.definitions.getEnemy(state.activeEnemy.enemyType).orElse(null);
        if (def == null || def.attack == null) return;

        state.enemyAttackCooldownRemaining = Math.max(0f, state.enemyAttackCooldownRemaining - delta);
        if (state.enemyAttackCooldownRemaining > 0f) return;

        // --- Rage check: enter rage once HP falls below threshold ---
        if (!state.activeEnemy.inRage && state.activeEnemy.maxHp > 0) {
            float hpFrac = (float) state.activeEnemy.hp / (float) state.activeEnemy.maxHp;
            if (hpFrac <= CombatTuning.RAGE_THRESHOLD) {
                state.activeEnemy.inRage = true;
                CombatLog.d("enemy RAGE type=" + state.activeEnemy.enemyType);
                if (feedback != null) feedback.onEnemyRage();
            }
        }

        int deployedFusions = 0;
        for (FusionInstance f : state.conveyorSockets) {
            if (f != null && f.hp > 0) deployedFusions++;
        }

        float baseInterval = def.attack.intervalSeconds();
        // Rage: attack faster
        if (state.activeEnemy.inRage) baseInterval *= CombatTuning.RAGE_INTERVAL_MULT;

        // Belt fill pressure: enemy attacks faster when belt is crowded.
        float fill = state.conveyorSockets.length <= 0 ? 0f : ((float) deployedFusions / state.conveyorSockets.length);
        float intervalCap = fill > 0.85f ? 1f : (fill >= 0.50f ? 2f : baseInterval);
        state.enemyAttackCooldownRemaining = Math.max(
                CombatTuning.MIN_ATTACK_INTERVAL_SECONDS,
                Math.min(baseInterval, intervalCap)
        );

        // --- Compute raw damage ---
        int extra = Math.max(0, deployedFusions - 1);
        float tier = getDifficultyTierFactor();
        float dynamicAtkMult = 1f + (CombatTuning.DYNAMIC_DIFFICULTY_PER_FUSION_EXTRA_ATK * tier) * extra;
        dynamicAtkMult *= computeFusionSpikeMultiplier(deployedFusions);
        if (state.activeEnemy.enemyType == EnemyType.BOSS_SMUGGLER_CAPTAIN) dynamicAtkMult *= CombatTuning.BOSS_BASE_ATK_MULT;
        if (state.endlessMode) dynamicAtkMult *= (1f + Math.max(0f, state.endlessAtkMultiplierBonus));
        if (state.activeEnemy.inRage) dynamicAtkMult *= CombatTuning.RAGE_ATK_MULT;

        int scaledDmg = Math.max(
                CombatTuning.MIN_DAMAGE,
                Math.round(def.attack.damage() * state.level.enemyAtkMultiplier * dynamicAtkMult * CombatTuning.ENEMY_DAMAGE_MULT)
        );

        // --- Tell (telegraph heavy strike) ---
        boolean isTell = context.random.chance(CombatTuning.TELL_CHANCE);
        if (isTell) {
            int heavyDmg = Math.round(scaledDmg * CombatTuning.TELL_DAMAGE_MULT);
            state.activeEnemy.chargingTell = CombatTuning.TELL_DURATION_SECONDS;
            state.activeEnemy.pendingTellDamage = heavyDmg;
            CombatLog.d("enemy TELL charge heavyDmg=" + heavyDmg);
            if (feedback != null) feedback.onEnemyTellStart();
            // Don't apply hit now — it fires in updateEnemyTell().
            return;
        }

        // --- Anti-carry stun ---
        boolean appliedStun = false;
        if (context.random.chance(CombatTuning.STUN_CHANCE)) {
            int highDpsSocket = findHighestDpsSocket();
            if (highDpsSocket >= 0) {
                if (state.activeEnemy.stunnedSocketId >= 0) {
                    // Already stunned; renew.
                }
                state.activeEnemy.stunRemaining = CombatTuning.STUN_DURATION_SECONDS;
                state.activeEnemy.stunnedSocketId = highDpsSocket;
                appliedStun = true;
                CombatLog.d("STUN applied to socket=" + highDpsSocket);
                if (feedback != null) feedback.onFusionStunned(highDpsSocket, CombatTuning.STUN_DURATION_SECONDS);
            }
        }

        // Normal attack (may still hit even if stun was applied — stun is a side effect).
        applyEnemyHit(scaledDmg, false);
    }

    /**
     * Apply an enemy hit: deducts armor first, then HP. Handles fusion/tube selection
     * and destruction feedback. Used by both direct attacks and tell-fire.
     */
    private void applyEnemyHit(int rawDmg, boolean isTellFire) {
        if (state.activeEnemy == null) return;

        // Armor absorbs damage first.
        int dmg = rawDmg;
        if (state.activeEnemy.armor > 0) {
            int absorbed = Math.min(state.activeEnemy.armor, dmg);
            state.activeEnemy.armor -= absorbed;
            dmg -= absorbed;
            if (state.activeEnemy.armor == 0) {
                CombatLog.d("enemy armor broken");
                if (feedback != null) feedback.onEnemyArmorBroken();
            }
            if (dmg <= 0) return; // fully absorbed
        }

        int targetSocket = findEnemyTargetSocket();
        FusionInstance target = targetSocket < 0 ? null : state.conveyorSockets[targetSocket];
        if (target == null) {
            state.tubeHp = Math.max(0, state.tubeHp - dmg);
            CombatLog.d("tube damaged amount=" + dmg + " tubeHp=" + state.tubeHp);
            if (feedback != null) feedback.onTubeDamaged(dmg);
            if (state.tubeHp <= 0) state.result = CombatResult.LOSE;
            return;
        }

        target.hp = Math.max(0, target.hp - dmg);
        CombatLog.d("fusion damaged amount=" + dmg + " hp=" + target.hp + "/" + target.maxHp);
        if (feedback != null) feedback.onFusionDamaged(true, targetSocket, dmg);
        if (target.hp <= 0) {
            state.conveyorSockets[targetSocket] = null;
            state.fusionAttackCooldownSockets[targetSocket] = 0f;
            if (state.activeEnemy != null && state.activeEnemy.stunnedSocketId == targetSocket) {
                state.activeEnemy.stunnedSocketId = -1;
                state.activeEnemy.stunRemaining = 0f;
            }
            CombatLog.d("fusion destroyed");
            if (feedback != null) feedback.onFusionDestroyed(true, targetSocket);
        }
    }

    /**
     * Role-based targeting AI.
     *
     * <ul>
     *   <li>BOSS / high-tier → TANK role → targets the fusion with the most HP (most durable threat).</li>
     *   <li>NET_THROWER / DRONE_THIEF → ASSASSIN role → targets highest DPS (the carry).</li>
     *   <li>GAS_BOMBER → MAGE role → picks randomly (splash feel; actual multi-hit in future).</li>
     *   <li>Others → default smart AI: highest DPS with 70% chance, random 30%.</li>
     * </ul>
     */
    private int findEnemyTargetSocket() {
        if (state.activeEnemy == null) return findRandomOccupiedSocket();

        return switch (state.activeEnemy.enemyType) {
            // TANK role: go for the beefiest fusion (highest max HP).
            case BOSS_SMUGGLER_CAPTAIN, SHIELD_SMUGGLER, BLACKMARKET_BRUTE -> findHighestHpSocket();
            // ASSASSIN role: always hits the highest-DPS fusion.
            case NET_THROWER, DRONE_THIEF -> findHighestDpsSocket();
            // MAGE/splash role: random target.
            case GAS_BOMBER -> findRandomOccupiedSocket();
            // Default: smart AI with 70% chance of highest DPS, 30% random.
            default -> context.random.chance(CombatTuning.ENEMY_TARGET_LOW_HP_CHANCE)
                    ? findHighestDpsSocket()
                    : findRandomOccupiedSocket();
        };
    }

    /** Returns the socket with the highest current DPS, or -1 if none. */
    private int findHighestDpsSocket() {
        int best = -1;
        float bestDps = -1f;
        for (int i = 0; i < state.conveyorSockets.length; i++) {
            FusionInstance f = state.conveyorSockets[i];
            if (f == null || f.hp <= 0 || f.stats == null) continue;
            float dps = f.stats.atk() / Math.max(0.05f, f.stats.attackIntervalSeconds());
            if (dps > bestDps) { bestDps = dps; best = i; }
        }
        return best >= 0 ? best : findRandomOccupiedSocket();
    }

    /** Returns the socket with the highest max HP, or -1 if none. */
    private int findHighestHpSocket() {
        int best = -1;
        int bestHp = -1;
        for (int i = 0; i < state.conveyorSockets.length; i++) {
            FusionInstance f = state.conveyorSockets[i];
            if (f == null || f.hp <= 0) continue;
            if (f.maxHp > bestHp) { bestHp = f.maxHp; best = i; }
        }
        return best >= 0 ? best : findRandomOccupiedSocket();
    }

    private int findRandomOccupiedSocket() {
        int count = 0;
        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance f = state.conveyorSockets[socketId];
            if (f != null && f.hp > 0) count++;
        }
        if (count == 0) return -1;

        int pick = context.random.nextInt(count);
        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance f = state.conveyorSockets[socketId];
            if (f == null || f.hp <= 0) continue;
            if (pick == 0) return socketId;
            pick--;
        }
        return -1;
    }

    private float getDynamicSpawnIntervalSeconds() {
        int deployedFusions = 0;
        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance f = state.conveyorSockets[socketId];
            if (f != null && f.hp > 0) deployedFusions++;
        }
        // Keep spawns readable: don't scale spawn interval too hard from fusions.
        int extra = Math.max(0, deployedFusions - 1);
        extra = Math.min(extra, 3);
        float tier = getDifficultyTierFactor();
        float mult = 1f - (CombatTuning.DYNAMIC_DIFFICULTY_SPAWN_INTERVAL_MULT_PER_FUSION * tier) * extra;
        mult = Math.max(CombatTuning.DYNAMIC_DIFFICULTY_MIN_SPAWN_INTERVAL_MULT, mult);
        mult = Math.min(CombatTuning.DYNAMIC_DIFFICULTY_MAX_SPAWN_INTERVAL_MULT, mult);
        return state.level.spawnIntervalSeconds * mult;
    }

    // =========================================================================
    // Tell (telegraph) — enemy winds up before big hit
    // =========================================================================

    private void updateEnemyTell(float delta) {
        if (state.activeEnemy == null) return;
        if (state.activeEnemy.chargingTell <= 0f) return;

        state.activeEnemy.chargingTell = Math.max(0f, state.activeEnemy.chargingTell - delta);
        if (state.activeEnemy.chargingTell <= 0f) {
            // Fire the heavy strike.
            int dmg = state.activeEnemy.pendingTellDamage;
            state.activeEnemy.pendingTellDamage = 0;
            applyEnemyHit(dmg, true);
            if (feedback != null) feedback.onEnemyTellFire(dmg);
        }
    }

    // =========================================================================
    // Stun tick — reduce stun timer each frame
    // =========================================================================

    private void tickStun(float delta) {
        if (state.activeEnemy == null) return;
        if (state.activeEnemy.stunRemaining > 0f) {
            state.activeEnemy.stunRemaining = Math.max(0f, state.activeEnemy.stunRemaining - delta);
        }
    }

    // =========================================================================
    // Mid-level buff application (called from UI when player picks)
    // =========================================================================

    /**
     * Apply the chosen mid-level buff. Call this from the screen after the player
     * taps an option. Resumes combat by clearing {@code midLevelChoicePending}.
     */
    public void applyMidLevelBuff(MidLevelBuff buff) {
        if (buff == null) return;
        switch (buff) {
            case FUSION_ATK_UP -> midLevelFusionAtkMult *= 1.25f;
            case TUBE_CHARGE -> {
                state.tubeCooldownRemaining = 0f;
                state.tubeCharges = state.tubeMaxCharges;
            }
            case FAST_COOLDOWN -> midLevelTubeCdMult *= 0.50f;
            case FUSION_HP_UP -> {
                for (FusionInstance f : state.conveyorSockets) {
                    if (f != null && f.maxHp > 0) {
                        f.hp = Math.min(f.maxHp, f.hp + Math.round(f.maxHp * 0.30f));
                    }
                }
            }
            case BELT_SPEED -> midLevelBeltSpeedBonus += 0.40f; // 40% faster belt
            case ENEMY_SLOW -> midLevelNextEnemyIntervalMult = 1.30f; // next enemy 30% slower
            case TIME_BONUS -> state.remainingTimeSeconds += 20f;
            case ARMOR_STRIP -> {
                if (state.activeEnemy != null) {
                    state.activeEnemy.armor = 0;
                }
            }
        }
        state.midLevelChoicePending = false;
        CombatLog.d("mid_level_buff applied=" + buff.name());
    }

    /** Expose the belt speed bonus so the view can adjust visual speed. */
    public float getMidLevelBeltSpeedBonus() { return midLevelBeltSpeedBonus; }

    // =========================================================================
    // Belt speed multiplier (used by view)
    // =========================================================================

    public float getBeltLoopSeconds() {
        float base = CombatTuning.CONVEYOR_LOOP_SECONDS;
        return base / Math.max(0.1f, 1f + midLevelBeltSpeedBonus);
    }

    // =========================================================================
    // Player DPS calculation for scaling
    // =========================================================================

    private float computeBeltTotalDps() {
        float total = 0f;
        for (FusionInstance f : state.conveyorSockets) {
            if (f == null || f.hp <= 0 || f.stats == null) continue;
            float atk = Math.max(0, f.stats.atk());
            float interval = Math.max(0.05f, f.stats.attackIntervalSeconds());
            total += atk / interval;
        }
        return total;
    }

    private void clearGrid() {
        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                state.grid[c][r] = null;
            }
        }
    }

    private void clearConveyor() {
        for (int i = 0; i < state.conveyorSockets.length; i++) {
            state.conveyorSockets[i] = null;
            state.fusionAttackCooldownSockets[i] = 0f;
            state.conveyorSocketPathIndex[i] = i;
        }
    }

    private String nextInstanceId() {
        instanceCounter++;
        return "i" + instanceCounter + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
