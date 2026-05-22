package com.splicelab.telemetry;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;

import java.util.ArrayDeque;
import java.util.Map;

public final class TelemetryBus {
    private final ArrayDeque<TelemetryEvent> queue = new ArrayDeque<>();

    public void track(String name, Map<String, Object> params) {
        queue.addLast(new TelemetryEvent(name, params, System.currentTimeMillis()));
    }

    // Call periodically from game loop; logs and clears.
    public void flush() {
        while (!queue.isEmpty()) {
            TelemetryEvent e = queue.removeFirst();
            Gdx.app.log(AppConstants.LOG_TAG, "TELEMETRY " + e.name + " " + e.params + " tsMs=" + e.timestampMs);
        }
    }
}

