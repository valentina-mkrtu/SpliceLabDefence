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
        state.tubeHp = level.tubeHp;

        float cd = level.tubeCooldownSeconds <= 0f ? context.config.tubeCooldownSeconds : level.tubeCooldownSeconds;
        int charges = level.maxTubeCharges <= 0 ? context.config.maxTubeCharges : level.maxTubeCharges;

        state.tubeCooldownRemaining = 0f;
        state.tubeCharges = charges;
        state.activeEnemy = null;
        state.enemySpawnCooldownRemaining = 0f;
        state.enemyAttackCooldownRemaining = 0f;
        state.conveyorStepCooldownRemaining = CombatTuning.CONVEYOR_STEP_INTERVAL_SECONDS;
        for (int i = 0; i < state.fusionAttackCooldownLeft.length; i++) state.fusionAttackCooldownLeft[i] = 0f;
        for (int i = 0; i < state.fusionAttackCooldownRight.length; i++) state.fusionAttackCooldownRight[i] = 0f;
        for (int i = 0; i < state.conveyorPathIndexLeft.length; i++) state.conveyorPathIndexLeft[i] = -1;
        for (int i = 0; i < state.conveyorPathIndexRight.length; i++) state.conveyorPathIndexRight[i] = -1;

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

        if (state.enemySpawnCooldownRemaining > 0f) {
            state.enemySpawnCooldownRemaining = Math.max(0f, state.enemySpawnCooldownRemaining - delta);
        }

        if (state.activeEnemy == null) {
            ensureEnemySpawned();
        }

        updateConveyorMovement(delta);

        updateFusionAutoAttack(delta);
        updateEnemyAttack(delta);
    }

    private void updateConveyorMovement(float delta) {
        state.conveyorStepCooldownRemaining = Math.max(0f, state.conveyorStepCooldownRemaining - delta);
        if (state.conveyorStepCooldownRemaining > 0f) return;
        state.conveyorStepCooldownRemaining = CombatTuning.CONVEYOR_STEP_INTERVAL_SECONDS;

        int pathLen = feedback == null ? 0 : feedback.getConveyorPathLength();
        if (pathLen <= 1) return;

        stepConveyorSide(true, state.conveyorLeft, state.conveyorPathIndexLeft, pathLen);
        stepConveyorSide(false, state.conveyorRight, state.conveyorPathIndexRight, pathLen);
    }

    private void stepConveyorSide(boolean leftSide, FusionInstance[] fusions, int[] indices, int pathLen) {
        boolean[] occupied = new boolean[pathLen];
        for (int i = 0; i < fusions.length; i++) {
            if (fusions[i] == null) continue;
            int idx = indices[i];
            if (idx >= 0 && idx < pathLen) occupied[idx] = true;
        }

        int[] nextIdx = new int[indices.length];
        for (int i = 0; i < indices.length; i++) nextIdx[i] = indices[i];

        for (int i = 0; i < fusions.length; i++) {
            if (fusions[i] == null) continue;
            int cur = indices[i];
            if (cur < 0) continue;
            int next = (cur + 1) % pathLen;
            if (occupied[next]) continue;
            occupied[cur] = false;
            occupied[next] = true;
            nextIdx[i] = next;
        }

        for (int i = 0; i < indices.length; i++) {
            if (nextIdx[i] != indices[i]) {
                indices[i] = nextIdx[i];
                if (feedback != null) feedback.onFusionMoved(leftSide, i, indices[i]);
            }
        }
    }

    public CommandResult requestTubeSpawn() {
        if (state.result != CombatResult.RUNNING) {
            return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        }
        if (!com.splicelab.debug.DebugFlags.FREE_TUBE_SPAWN && state.tubeCooldownRemaining > 0f) {
            return CommandResult.fail(CommandResult.Code.TUBE_ON_COOLDOWN, "Tube on cooldown");
        }
        if (state.tubeCharges <= 0) {
            return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "No tube charges");
        }

        int[] empty = findRandomEmptyNonTubeCell();
        if (empty == null) {
            return CommandResult.fail(CommandResult.Code.NO_EMPTY_GRID_CELL, "No empty grid cell");
        }

        var choice = context.tubeSpawnService.chooseSpawnForLevel(state.level.levelNumber);
        if (choice.type() == com.splicelab.services.TubeSpawnService.SpawnChoice.Type.NONE) {
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
        float cd = state.level.tubeCooldownSeconds <= 0f ? context.config.tubeCooldownSeconds : state.level.tubeCooldownSeconds;
        state.tubeCooldownRemaining = cd;
        state.tubeCharges = Math.max(0, state.tubeCharges - 1);
        CombatLog.d("spawn ingredient type=" + choice.type() + " at=" + empty[0] + "," + empty[1]);
        return CommandResult.ok();
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
            CombatLog.d("fusion created at=" + fromCol + "," + fromRow);
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

        state.grid[colA][rowA] = fusion;
        state.grid[colB][rowB] = null;
        return CommandResult.ok();
    }

    public CommandResult requestDeployFusionFromGrid(int fromCol, int fromRow, boolean leftSide, int slotIndex) {
        if (state.result != CombatResult.RUNNING) return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        if (!isValidCell(fromCol, fromRow) || isTubeCell(fromCol, fromRow)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Invalid cell");

        IngredientInstance src = state.grid[fromCol][fromRow];
        if (!(src instanceof FusionInstance fusion)) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Not a fusion");

        if (!context.unlocks.isConveyorSlotUnlocked(leftSide, slotIndex)) {
            return CommandResult.fail(CommandResult.Code.SLOT_LOCKED, "Slot locked");
        }

        FusionInstance[] arr = leftSide ? state.conveyorLeft : state.conveyorRight;
        if (slotIndex < 0 || slotIndex >= arr.length) return CommandResult.fail(CommandResult.Code.INVALID_PAYLOAD, "Bad slot");
        if (arr[slotIndex] != null) return CommandResult.fail(CommandResult.Code.SLOT_OCCUPIED, "Slot occupied");

        arr[slotIndex] = fusion;
        state.grid[fromCol][fromRow] = null;

        if (feedback != null) {
            int pathLen = feedback.getConveyorPathLength();
            int startIndex = feedback.mapSlotToPathIndex(leftSide, slotIndex);
            if (isPathIndexOccupied(startIndex, pathLen)) {
                arr[slotIndex] = null;
                state.grid[fromCol][fromRow] = fusion;
                return CommandResult.fail(CommandResult.Code.SLOT_OCCUPIED, "Path occupied");
            }
            if (leftSide) state.conveyorPathIndexLeft[slotIndex] = startIndex;
            else state.conveyorPathIndexRight[slotIndex] = startIndex;
            feedback.onFusionMoved(leftSide, slotIndex, startIndex);
        }
        CombatLog.d("fusion deployed side=" + (leftSide ? "L" : "R") + " slot=" + slotIndex);
        return CommandResult.ok();
    }

    private boolean isPathIndexOccupied(int idx, int pathLen) {
        if (idx < 0 || idx >= pathLen) return false;
        for (int i = 0; i < state.conveyorLeft.length; i++) {
            if (state.conveyorLeft[i] != null && state.conveyorPathIndexLeft[i] == idx) return true;
        }
        for (int i = 0; i < state.conveyorRight.length; i++) {
            if (state.conveyorRight[i] != null && state.conveyorPathIndexRight[i] == idx) return true;
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

        for (int i = 0; i < state.conveyorLeft.length; i++) {
            FusionInstance fusion = state.conveyorLeft[i];
            if (fusion == null) continue;
            state.fusionAttackCooldownLeft[i] = Math.max(0f, state.fusionAttackCooldownLeft[i] - delta);
            if (state.fusionAttackCooldownLeft[i] > 0f) continue;
            attackEnemyFromFusion(true, i, fusion);
            state.fusionAttackCooldownLeft[i] = fusion.stats.attackIntervalSeconds();
        }
        for (int i = 0; i < state.conveyorRight.length; i++) {
            FusionInstance fusion = state.conveyorRight[i];
            if (fusion == null) continue;
            state.fusionAttackCooldownRight[i] = Math.max(0f, state.fusionAttackCooldownRight[i] - delta);
            if (state.fusionAttackCooldownRight[i] > 0f) continue;
            attackEnemyFromFusion(false, i, fusion);
            state.fusionAttackCooldownRight[i] = fusion.stats.attackIntervalSeconds();
        }
    }

    private void attackEnemyFromFusion(boolean leftSide, int slotIndex, FusionInstance fusion) {
        if (state.activeEnemy == null) return;

        if (feedback != null) {
            int pathIndex = leftSide ? state.conveyorPathIndexLeft[slotIndex] : state.conveyorPathIndexRight[slotIndex];
            if (pathIndex != CombatTuning.ATTACK_ZONE_INDEX) return;
            CombatLog.d("fusion at attack zone side=" + (leftSide ? "L" : "R") + " slot=" + slotIndex);
        }

        int base = Math.max(0, fusion.stats.atk());
        float variance = Math.max(0f, fusion.stats.variance());
        float roll = (rng.nextFloat() * 2f - 1f) * variance;
        int dmg = Math.max(CombatTuning.MIN_DAMAGE, Math.round(base * (1f + roll)));

        boolean special = rng.nextFloat() < fusion.stats.specialChance();
        if (special) dmg *= 2;

        CombatLog.d("fusion hit enemy dmg=" + dmg + (special ? " SPECIAL" : ""));

        if (feedback != null) {
            feedback.onFusionAttack(leftSide, slotIndex, dmg, special);
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

        FusionInstance target = findFirstDeployedFusion();
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
            int[] idx = findFusionSlotIndex(target);
            if (idx != null) feedback.onFusionDamaged(idx[0] == 1, idx[1], scaledDmg);
        }
        if (target.hp <= 0) {
            int[] idx = findFusionSlotIndex(target);
            removeFusionInstance(target);
            CombatLog.d("fusion destroyed");
            if (feedback != null && idx != null) feedback.onFusionDestroyed(idx[0] == 1, idx[1]);
        }
    }

    private FusionInstance findFirstDeployedFusion() {
        for (FusionInstance f : state.conveyorLeft) if (f != null && f.hp > 0) return f;
        for (FusionInstance f : state.conveyorRight) if (f != null && f.hp > 0) return f;
        return null;
    }

    private int[] findFusionSlotIndex(FusionInstance instance) {
        for (int i = 0; i < state.conveyorLeft.length; i++) {
            if (state.conveyorLeft[i] == instance) return new int[]{1, i};
        }
        for (int i = 0; i < state.conveyorRight.length; i++) {
            if (state.conveyorRight[i] == instance) return new int[]{0, i};
        }
        return null;
    }

    private void removeFusionInstance(FusionInstance instance) {
        for (int i = 0; i < state.conveyorLeft.length; i++) {
            if (state.conveyorLeft[i] == instance) {
                state.conveyorLeft[i] = null;
                state.fusionAttackCooldownLeft[i] = 0f;
                state.conveyorPathIndexLeft[i] = -1;
                return;
            }
        }
        for (int i = 0; i < state.conveyorRight.length; i++) {
            if (state.conveyorRight[i] == instance) {
                state.conveyorRight[i] = null;
                state.fusionAttackCooldownRight[i] = 0f;
                state.conveyorPathIndexRight[i] = -1;
                return;
            }
        }
    }

    private void clearGrid() {
        for (int c = 0; c < AppConstants.GRID_COLS; c++) {
            for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
                state.grid[c][r] = null;
            }
        }
    }

    private void clearConveyor() {
        for (int i = 0; i < state.conveyorLeft.length; i++) state.conveyorLeft[i] = null;
        for (int i = 0; i < state.conveyorRight.length; i++) state.conveyorRight[i] = null;
        for (int i = 0; i < state.conveyorPathIndexLeft.length; i++) state.conveyorPathIndexLeft[i] = -1;
        for (int i = 0; i < state.conveyorPathIndexRight.length; i++) state.conveyorPathIndexRight[i] = -1;
    }

    private String nextInstanceId() {
        instanceCounter++;
        return "i" + instanceCounter + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
