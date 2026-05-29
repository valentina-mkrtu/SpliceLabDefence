package com.splicelab.telemetry;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;
import com.splicelab.debug.DebugFlags;

import java.util.ArrayDeque;
import java.util.Map;

/**
 * Lightweight event bus for telemetry.
 *
 * <p>In the current build telemetry is debug-only: {@link #track} and {@link #flush} are
 * no-ops unless {@link DebugFlags#DEBUG} is true.  (T-4.4)</p>
 *
 * <p>To add real analytics: implement a platform-specific transport behind an interface,
 * add {@code INTERNET} permission to {@code AndroidManifest.xml}, batch events off the
 * render thread, and document your data-collection policy.</p>
 */
public final class TelemetryBus {
    private final ArrayDeque<TelemetryEvent> queue = new ArrayDeque<>();

    /** Enqueues an event. No-op in release builds. */
    public void track(String name, Map<String, Object> params) {
        if (!DebugFlags.DEBUG) return;
        queue.addLast(new TelemetryEvent(name, params, System.currentTimeMillis()));
    }

    /**
     * Logs and clears queued events.
     * Call at natural checkpoints (level end, lobby), NOT every frame. (T-4.4)
     */
    public void flush() {
        if (!DebugFlags.DEBUG) return;
        while (!queue.isEmpty()) {
            TelemetryEvent e = queue.removeFirst();
            Gdx.app.log(AppConstants.LOG_TAG, "TELEMETRY " + e.name + " " + e.params + " tsMs=" + e.timestampMs);
        }
    }
}

