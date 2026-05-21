package com.splicelab.combat;

public final class CommandResult {
    public enum Code {
        OK,
        ROUND_NOT_RUNNING,
        CELL_OCCUPIED,
        CELL_EMPTY,
        INVALID_FUSION,
        SLOT_LOCKED,
        SLOT_OCCUPIED,
        TUBE_ON_COOLDOWN,
        NO_EMPTY_GRID_CELL,
        INVALID_PAYLOAD,
        INVALID_LEVEL
    }

    public final boolean success;
    public final Code code;
    public final String message;

    private CommandResult(boolean success, Code code, String message) {
        this.success = success;
        this.code = code;
        this.message = message == null ? "" : message;
    }

    public static CommandResult ok() {
        return new CommandResult(true, Code.OK, "OK");
    }

    public static CommandResult fail(Code code, String message) {
        return new CommandResult(false, code, message);
    }
}

