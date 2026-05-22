package com.splicelab.telemetry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TelemetryEvent {
    public final String name;
    public final Map<String, Object> params;
    public final long timestampMs;

    public TelemetryEvent(String name, Map<String, Object> params, long timestampMs) {
        this.name = name == null ? "" : name;
        this.params = params == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(params));
        this.timestampMs = timestampMs;
    }
}

