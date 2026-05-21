package com.splicelab.data;

import com.splicelab.app.GameConfig;

public final class SaveMigrationService {
    private final GameConfig config;

    public SaveMigrationService(GameConfig config) {
        this.config = config;
    }

    public SaveData migrateIfNeeded(SaveData data) {
        if (data == null) return null;
        // v1 only for now.
        if (data.schemaVersion != config.saveSchemaVersion) {
            data.schemaVersion = config.saveSchemaVersion;
        }
        return data;
    }
}

