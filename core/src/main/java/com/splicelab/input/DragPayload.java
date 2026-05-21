package com.splicelab.input;

public final class DragPayload {
    public enum PayloadType { INGREDIENT, FUSION }

    public enum SourceType { GRID_CELL, CONVEYOR_SLOT }

    public final PayloadType payloadType;
    public final SourceType sourceType;
    public final String sourceId;
    public final String instanceId;

    public DragPayload(PayloadType payloadType, SourceType sourceType, String sourceId, String instanceId) {
        this.payloadType = payloadType;
        this.sourceType = sourceType;
        this.sourceId = sourceId == null ? "" : sourceId;
        this.instanceId = instanceId == null ? "" : instanceId;
    }
}

