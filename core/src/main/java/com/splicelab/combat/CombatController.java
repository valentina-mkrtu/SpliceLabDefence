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

    private final java.util.Random rng = new java.util.Random();

    private static final int DEFAULT_TUBE_BAG_SIZE = 8;
    private final java.util.ArrayDeque<com.splicelab.services.TubeSpawnService.SpawnChoice> tubeBag = new java.util.ArrayDeque<>();

    public interface CombatFeedback {
        int getConveyorPathLength();

        int mapSlotToPathIndex(boolean leftSide, int slotIndex);

        void onFusionMoved(boolean leftSide, int slotIndex, int pathIndex);

        void onFusionAttack(boolean leftSide, int slotIndex, int damage, boolean special);

        void onEnemyDamaged(int damage, boolean special);

        void onEnemyDefeated();

        void onFusionDamaged(boolean leftSide, int slotIndex, int damage);

        void onFusionDestroyed(boolean leftSide, int slotIndex);

        void onTubeDamaged(int damage);
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
        LevelDefinition level = context.levels.getLevel(levelNumber).orElse(null);
        if (level == null) {
            CombatLog.d("Missing level " + levelNumber);
            state.result = CombatResult.LOSE;
            return state;
        }

        clearGrid();
        clearConveyor();

        state.level = level;
        state.remainingTimeSeconds = level.durationSeconds;
        state.tubeHp = level.tubeHp > 0 ? level.tubeHp : context.config.tubeMaxHp;

        float cd = level.tubeCooldownSeconds <= 0f ? context.config.tubeCooldownSeconds : level.tubeCooldownSeconds;
        int charges = level.maxTubeCharges <= 0 ? context.config.maxTubeCharges : level.maxTubeCharges;
        charges = Math.max(1, charges);
        cd = Math.max(0.25f, cd);

        state.tubeCooldownRemaining = 0f;
        state.tubeMaxCharges = charges;
        state.tubeCharges = charges;
        refillTubeBagIfNeeded();
        state.activeEnemy = null;
        state.enemySpawnCooldownRemaining = 0f;
        // Enemy attacks every 3 seconds.
        state.enemyAttackCooldownRemaining = 3f;
        state.conveyorStepCooldownRemaining = CombatTuning.CONVEYOR_STEP_INTERVAL_SECONDS;
        for (int i = 0; i < state.fusionAttackCooldownSockets.length; i++) state.fusionAttackCooldownSockets[i] = 0f;

        state.result = CombatResult.RUNNING;

        context.saves.get().unlockedConveyorSlotsLeft = Math.max(context.saves.get().unlockedConveyorSlotsLeft, level.unlockedConveyorSlotsLeft);
        context.saves.get().unlockedConveyorSlotsRight = Math.max(context.saves.get().unlockedConveyorSlotsRight, level.unlockedConveyorSlotsRight);
        context.saves.save();

        CombatLog.d("LEVEL_START level=" + level.levelNumber);
        CombatLog.d("allowedEntities=" + level.availableEntities);
        CombatLog.d("allowedItems=" + level.availableItems);
        CombatLog.d("enemyPool=" + level.enemyPool);
        CombatLog.d("tubeHp=" + level.tubeHp + " durationSeconds=" + level.durationSeconds);
        CombatLog.d("rewards coins=" + level.rewards.coins() + " dna=" + level.rewards.dna());
        CombatLog.d("tubeCooldownSeconds=" + cd + " maxTubeCharges=" + charges);

        ensureEnemySpawned();
        return state;
    }

    public CombatState getState() {
        return state;
    }

    public void update(float delta) {
        if (state.result != CombatResult.RUNNING) return;

        if (state.remainingTimeSeconds > 0f) {
            state.remainingTimeSeconds = Math.max(0f, state.remainingTimeSeconds - delta * (com.splicelab.debug.DebugFlags.FAST_ROUND_TIMER ? 3f : 1f));
            if (state.remainingTimeSeconds <= 0f) {
                state.result = CombatResult.WIN;
                return;
            }
        }

        if (state.tubeCooldownRemaining > 0f) {
            state.tubeCooldownRemaining = Math.max(0f, state.tubeCooldownRemaining - delta);
        }

        // Tube cooldown refill: cooldown only happens when empty.
        if (state.level != null && state.tubeCharges <= 0) {
            if (state.tubeCooldownRemaining <= 0f) {
                state.tubeCharges = state.tubeMaxCharges;
                refillTubeBagIfNeeded();
            }
        }

        if (state.enemySpawnCooldownRemaining > 0f) {
            state.enemySpawnCooldownRemaining = Math.max(0f, state.enemySpawnCooldownRemaining - delta);
        }

        if (state.activeEnemy == null) {
            ensureEnemySpawned();
        }

        updateConveyorPhase(delta);
        // Conveyor sockets are visual belt pockets; they don't advance occupancy.

        updateFusionAutoAttack(delta);
        updateEnemyAttack(delta);
    }

    private void updateConveyorPhase(float delta) {
        float loopSeconds = Math.max(0.01f, CombatTuning.CONVEYOR_LOOP_SECONDS);
        state.conveyorBeltPhase += delta / loopSeconds;
        if (state.conveyorBeltPhase >= 1f) state.conveyorBeltPhase -= (float) Math.floor(state.conveyorBeltPhase);
    }

    private void updateConveyorMovement(float delta) {
        // Intentionally left blank: sockets stay in their assigned pocket.
    }

    public CommandResult requestTubeSpawn() {
        if (state.result != CombatResult.RUNNING) {
            return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        }
        if (!com.splicelab.debug.DebugFlags.FREE_TUBE_SPAWN && state.tubeCooldownRemaining > 0f) {
            return CommandResult.fail(CommandResult.Code.TUBE_ON_COOLDOWN, "Tube on cooldown");
        }
        if (state.tubeCharges <= 0) {
            // Start cooldown when empty.
            if (state.tubeCooldownRemaining <= 0f) state.tubeCooldownRemaining = getTubeCooldownSeconds();
            return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "No tube charges");
        }

        int[] empty = findRandomEmptyNonTubeCell();
        if (empty == null) return CommandResult.fail(CommandResult.Code.NO_EMPTY_GRID_CELL, "No empty grid cell");

        refillTubeBagIfNeeded();
        var choice = tubeBag.pollFirst();
        if (choice == null || choice.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.NONE) {
            return CommandResult.fail(CommandResult.Code.INVALID_LEVEL, "No spawn choices");
        }

        String id = nextInstanceId();
        IngredientInstance instance;
        if (choice.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.ENTITY) {
            EntityType e = choice.entityType();
            instance = SimpleIngredientInstance.ofEntity(id, e);
        } else {
            ItemType i = choice.itemType();
            instance = SimpleIngredientInstance.ofItem(id, i);
        }

        state.grid[empty[0]][empty[1]] = instance;
        state.tubeCharges = Math.max(0, state.tubeCharges - 1);

        // Cooldown only after the last spit.
        if (state.tubeCharges <= 0) state.tubeCooldownRemaining = getTubeCooldownSeconds();

        CombatLog.d("spawn ingredient type=" + choice.type() + " at=" + empty[0] + "," + empty[1]);
        return CommandResult.ok();
    }

    private float getTubeCooldownSeconds() {
        float cd = state.level == null ? context.config.tubeCooldownSeconds : state.level.tubeCooldownSeconds;
        if (cd <= 0f) cd = context.config.tubeCooldownSeconds;
        return Math.max(0.25f, cd);
    }

    private void refillTubeBagIfNeeded() {
        if (!tubeBag.isEmpty()) return;
        int bagSize = Math.max(1, state.tubeMaxCharges > 0 ? state.tubeMaxCharges : DEFAULT_TUBE_BAG_SIZE);
        tubeBag.addAll(context.tubeSpawnService.buildSpawnBagForLevel(state.level.levelNumber, bagSize));
    }

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
        return CommandResult.fail(CommandResult.Code.CELL_OCCUPIED, "Target occupied");
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

        FusionInstance fusion = context.fusionService.createFusion(nextInstanceId(), entity.entityType(), item.itemType()).orElse(null);
        if (fusion == null) return CommandResult.fail(CommandResult.Code.INVALID_FUSION, "Fusion creation failed");

        state.grid[colB][rowB] = fusion;
        state.grid[colA][rowA] = null;
        return CommandResult.ok();
    }

    public CommandResult requestDeployFusionFromGrid(int fromCol, int fromRow, boolean leftSide, int slotIndex) {
        // Deprecated: left/right slot deployment replaced by 12-socket deployment.
        return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Use socket deployment");
    }

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
        state.activeEnemy.hp = Math.max(0, state.activeEnemy.hp - Math.max(1, amount));
        if (state.activeEnemy.hp <= 0) {
            CombatLog.d("enemy defeated type=" + state.activeEnemy.enemyType);
            state.activeEnemy = null;
            state.enemySpawnCooldownRemaining = state.level.spawnIntervalSeconds;
        }
    }

    private boolean isTubeCell(int c, int r) {
        return c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW;
    }

    private boolean isValidCell(int c, int r) {
        return c >= 0 && c < AppConstants.GRID_COLS && r >= 0 && r < AppConstants.GRID_ROWS;
    }

    private int[] findRandomEmptyNonTubeCell() {
        int emptyCount = 0;
        for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                if (isTubeCell(c, r)) continue;
                if (state.grid[c][r] == null) emptyCount++;
            }
        }
        if (emptyCount == 0) return null;

        int pick = rng.nextInt(emptyCount);
        for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                if (isTubeCell(c, r)) continue;
                if (state.grid[c][r] != null) continue;
                if (pick == 0) return new int[]{c, r};
                pick--;
            }
        }
        return null;
    }

    private void ensureEnemySpawned() {
        if (state.level == null) return;
        if (state.activeEnemy != null) return;
        if (state.enemySpawnCooldownRemaining > 0f) return;
        if (state.level.enemyPool.isEmpty()) return;

        EnemyType chosenType = chooseWeightedEnemyType(state.level);
        EnemyDefinition def = context.definitions.getEnemy(chosenType).orElse(null);
        if (def == null) return;

        int scaledHp = Math.max(1, Math.round(def.maxHp * state.level.enemyHpMultiplier));
        state.activeEnemy = new EnemyInstance(nextInstanceId(), chosenType, scaledHp);
        state.enemyAttackCooldownRemaining = def.attack.intervalSeconds();
        CombatLog.d("enemy spawned type=" + chosenType + " hp=" + scaledHp);
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
            state.fusionAttackCooldownSockets[socketId] = Math.max(0f, state.fusionAttackCooldownSockets[socketId] - delta);
            if (state.fusionAttackCooldownSockets[socketId] > 0f) continue;
            attackEnemyFromFusionSocket(socketId, fusion);
            state.fusionAttackCooldownSockets[socketId] = fusion.stats.attackIntervalSeconds();
        }
    }

    private boolean isSocketAtAttackCheckpoint(int socketId) {
        int pathLen = feedback == null ? 0 : feedback.getConveyorPathLength();
        if (pathLen <= 0) return false;

        float phase = state.conveyorBeltPhase;
        float idxF = (phase * pathLen) % pathLen;
        int beltIndex = ((int) Math.floor(idxF)) % pathLen;
        int socketIndex = ((socketId % pathLen) + pathLen) % pathLen;
        int checkpointIndex = ((CombatTuning.ATTACK_ZONE_INDEX % pathLen) + pathLen) % pathLen;
        return ((socketIndex + beltIndex) % pathLen) == checkpointIndex;
    }

    private void attackEnemyFromFusionSocket(int socketId, FusionInstance fusion) {
        if (state.activeEnemy == null) return;

        if (!isSocketAtAttackCheckpoint(socketId)) return;
        CombatLog.d("fusion at attack zone socket=" + socketId);

        int base = Math.max(0, fusion.stats.atk());
        float variance = Math.max(0f, fusion.stats.variance());
        float roll = (rng.nextFloat() * 2f - 1f) * variance;
        int dmg = Math.max(CombatTuning.MIN_DAMAGE, Math.round(base * (1f + roll)));

        boolean special = rng.nextFloat() < fusion.stats.specialChance();
        if (special) dmg *= 2;

        CombatLog.d("fusion hit enemy dmg=" + dmg + (special ? " SPECIAL" : ""));

        if (feedback != null) {
            // Map socket-based attacks through the new socket anchor.
            feedback.onFusionAttack(true, socketId, dmg, special);
            feedback.onEnemyDamaged(dmg, special);
        }

        state.activeEnemy.hp = Math.max(0, state.activeEnemy.hp - dmg);
        if (state.activeEnemy.hp <= 0) {
            CombatLog.d("enemy defeated type=" + state.activeEnemy.enemyType);
            if (feedback != null) feedback.onEnemyDefeated();
            state.activeEnemy = null;
            state.enemySpawnCooldownRemaining = state.level.spawnIntervalSeconds + CombatTuning.ENEMY_SPAWN_DELAY_AFTER_DEATH_SECONDS;
        }
    }

    private void updateEnemyAttack(float delta) {
        if (state.level == null) return;
        if (state.activeEnemy == null) return;

        EnemyDefinition def = context.definitions.getEnemy(state.activeEnemy.enemyType).orElse(null);
        if (def == null || def.attack == null) return;

        state.enemyAttackCooldownRemaining = Math.max(0f, state.enemyAttackCooldownRemaining - delta);
        if (state.enemyAttackCooldownRemaining > 0f) return;
        state.enemyAttackCooldownRemaining = def.attack.intervalSeconds();

        int scaledDmg = Math.max(CombatTuning.MIN_DAMAGE, Math.round(def.attack.damage() * state.level.enemyAtkMultiplier));

        int targetSocket = findRandomOccupiedSocket();
        FusionInstance target = targetSocket < 0 ? null : state.conveyorSockets[targetSocket];
        if (target == null) {
            state.tubeHp = Math.max(0, state.tubeHp - scaledDmg);
            CombatLog.d("tube damaged amount=" + scaledDmg + " tubeHp=" + state.tubeHp);
            if (feedback != null) feedback.onTubeDamaged(scaledDmg);
            if (state.tubeHp <= 0) {
                state.result = CombatResult.LOSE;
            }
            return;
        }

        target.hp = Math.max(0, target.hp - scaledDmg);
        CombatLog.d("fusion damaged amount=" + scaledDmg + " hp=" + target.hp + "/" + target.maxHp);
        if (feedback != null) {
            feedback.onFusionDamaged(true, targetSocket, scaledDmg);
        }
        if (target.hp <= 0) {
            state.conveyorSockets[targetSocket] = null;
            state.fusionAttackCooldownSockets[targetSocket] = 0f;
            CombatLog.d("fusion destroyed");
            if (feedback != null) feedback.onFusionDestroyed(true, targetSocket);
        }
    }

    private int findRandomOccupiedSocket() {
        int count = 0;
        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance f = state.conveyorSockets[socketId];
            if (f != null && f.hp > 0) count++;
        }
        if (count == 0) return -1;

        int pick = rng.nextInt(count);
        for (int socketId = 0; socketId < state.conveyorSockets.length; socketId++) {
            FusionInstance f = state.conveyorSockets[socketId];
            if (f == null || f.hp <= 0) continue;
            if (pick == 0) return socketId;
            pick--;
        }
        return -1;
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
