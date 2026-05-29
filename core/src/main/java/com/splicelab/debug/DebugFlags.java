package com.splicelab.debug;

/**
 * Central debug-flag master switch.
 *
 * <p>Set {@link #DEBUG} to {@code true} only during development.
 * All individual flags are gated on it so they can never accidentally be left on in a
 * release build — R8 will constant-fold and dead-strip the entire gated code paths
 * when {@code DEBUG = false}.</p>
 */
public final class DebugFlags {

    /**
     * Master switch — set to {@code false} before every release build.
     * When {@code false}, every flag below evaluates to {@code false} regardless of its own value.
     */
    public static final boolean DEBUG = false;

    // Individual flags — only effective when DEBUG == true.
    public static final boolean ENABLE_DEBUG_OVERLAY = DEBUG && true;
    public static final boolean FAST_ROUND_TIMER     = DEBUG && false;
    public static final boolean UNLOCK_ALL           = DEBUG && false;
    public static final boolean FREE_TUBE_SPAWN      = DEBUG && false;
    public static final boolean SHOW_HITBOXES        = DEBUG && false;
    public static final boolean SEEDED_RANDOM        = DEBUG && false;

    private DebugFlags() {}
}

