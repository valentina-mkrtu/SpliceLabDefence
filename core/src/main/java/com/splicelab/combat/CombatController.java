package com.splicelab.combat;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameContext;
import com.splicelab.model.EntityType;
import com.splicelab.model.ItemType;
import com.splicelab.model.ingredient.FusionInstance;
import com.splicelab.model.ingredient.IngredientInstance;
import com.splicelab.model.ingredient.SimpleIngredientInstance;
import com.splicelab.model.level.LevelDefinition;

import java.util.UUID;

public final class CombatController {
    private final GameContext context;
    private final CombatState state;
    private int instanceCounter;

    public CombatController(GameContext context) {
        this.context = context;
        this.state = new CombatState(context.config.maxConveyorSlotsPerSide);
    }

    public CombatState startLevel(int levelNumber) {
        LevelDefinition level = context.levels.getLevel(levelNumber).orElse(null);
        if (level == null) {
            Gdx.app.error(AppConstants.LOG_TAG, "Missing level " + levelNumber);
            state.result = CombatResult.LOSE;
            return state;
        }

        clearGrid();
        clearConveyor();

        state.level = level;
        state.remainingTimeSeconds = level.durationSeconds;
        state.tubeHp = level.tubeHp;
        state.tubeCooldownRemaining = 0f;
        state.activeEnemy = null;
        state.result = CombatResult.RUNNING;
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
    }

    public CommandResult requestTubeSpawn() {
        if (state.result != CombatResult.RUNNING) {
            return CommandResult.fail(CommandResult.Code.ROUND_NOT_RUNNING, "Round not running");
        }
        if (!com.splicelab.debug.DebugFlags.FREE_TUBE_SPAWN && state.tubeCooldownRemaining > 0f) {
            return CommandResult.fail(CommandResult.Code.TUBE_ON_COOLDOWN, "Tube on cooldown");
        }

        int[] empty = findFirstEmptyNonTubeCell();
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
        state.tubeCooldownRemaining = 0.6f;
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
        return CommandResult.ok();
    }

    private boolean isTubeCell(int c, int r) {
        return c == AppConstants.TUBE_COL && r == AppConstants.TUBE_ROW;
    }

    private boolean isValidCell(int c, int r) {
        return c >= 0 && c < AppConstants.GRID_COLS && r >= 0 && r < AppConstants.GRID_ROWS;
    }

    private int[] findFirstEmptyNonTubeCell() {
        for (int r = 0; r < AppConstants.GRID_ROWS; r++) {
            for (int c = 0; c < AppConstants.GRID_COLS; c++) {
                if (isTubeCell(c, r)) continue;
                if (state.grid[c][r] == null) return new int[]{c, r};
            }
        }
        return null;
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
    }

    private String nextInstanceId() {
        instanceCounter++;
        return "i" + instanceCounter + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}

