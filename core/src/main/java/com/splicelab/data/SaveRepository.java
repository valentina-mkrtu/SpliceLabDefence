package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameConfig;

public final class SaveRepository {
    private static final String PREFS_NAME = "splicelab_save";
    private static final String KEY_SAVE_JSON = "save_json";
    private static final String KEY_BACKUP_JSON = "backup_json";

    private final Preferences prefs;
    private final Json json;
    private final SaveValidator validator;
    private final SaveMigrationService migrationService;
    private SaveData current;

    public SaveRepository(GameConfig config) {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.json = new Json();
        this.json.setOutputType(JsonWriter.OutputType.json);
        this.validator = new SaveValidator(config);
        this.migrationService = new SaveMigrationService(config);
    }

    public SaveData get() {
        if (current == null) {
            current = validator.defaultSave();
        }
        return current;
    }

    public void loadOrCreate() {
        SaveData loaded = null;
        try {
            String raw = prefs.getString(KEY_SAVE_JSON, "");
            if (raw != null && !raw.isBlank()) {
                loaded = json.fromJson(SaveData.class, raw);
            }
        } catch (Exception ex) {
            Gdx.app.error(AppConstants.LOG_TAG, "Failed to load save; will recover", ex);
        }

        loaded = migrationService.migrateIfNeeded(loaded);
        current = validator.validateAndRepair(loaded);
        save();
    }

    public void save() {
        SaveData data = validator.validateAndRepair(get());
        String raw = json.toJson(data);
        try {
            prefs.putString(KEY_BACKUP_JSON, prefs.getString(KEY_SAVE_JSON, ""));
            prefs.putString(KEY_SAVE_JSON, raw);
            prefs.flush();
        } catch (Exception ex) {
            Gdx.app.error(AppConstants.LOG_TAG, "Failed to save preferences", ex);
        }
    }

    public void reset() {
        current = validator.defaultSave();
        save();
    }
}

