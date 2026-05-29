package com.splicelab.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.splicelab.app.AppConstants;
import com.splicelab.app.GameConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Persists {@link SaveData} to libGDX Preferences with:
 * <ul>
 *   <li>HMAC-SHA256 integrity signature — detects tampering and falls back to a fresh save.</li>
 *   <li>Base64 obfuscation — stops trivial "open file and edit" attacks.</li>
 *   <li>Dirty-flag batching — use {@link #markDirty()} / {@link #flushIfDirty()} instead of
 *       calling {@link #save()} on every mutation; reserve {@link #save()} for durable checkpoints.</li>
 * </ul>
 *
 * <p>The HMAC secret lives in the APK and can be extracted by a determined attacker.
 * The intent is to stop casual editing, not to be cryptographically unbreakable.
 * Combine with R8 obfuscation for meaningful additional protection.</p>
 */
public final class SaveRepository {
    private static final String PREFS_NAME       = "splicelab_save";
    private static final String KEY_SAVE_JSON    = "save_json";
    private static final String KEY_BACKUP_JSON  = "backup_json";
    private static final String KEY_SAVE_SIG     = "save_sig";

    /**
     * App-level HMAC secret.  Rotate this string to invalidate all existing saves on the next
     * schema migration if you ever need to break compatibility.
     */
    private static final String HMAC_SECRET = "SpliceLabSaveKey_v1";

    private final Preferences prefs;
    private final Json json;
    private final SaveValidator validator;
    private final SaveMigrationService migrationService;
    private SaveData current;
    private boolean dirty = false;

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

    // -------------------------------------------------------------------------
    // Load / save
    // -------------------------------------------------------------------------

    public void loadOrCreate() {
        SaveData loaded = null;
        try {
            String encoded = prefs.getString(KEY_SAVE_JSON, "");
            String storedSig = prefs.getString(KEY_SAVE_SIG, "");
            if (encoded != null && !encoded.isBlank()) {
                // Verify integrity before decoding
                if (!sign(encoded).equals(storedSig)) {
                    Gdx.app.error(AppConstants.LOG_TAG,
                            "Save integrity check failed — signature mismatch. Falling back to default save.");
                    loaded = null;
                } else {
                    String raw = decode(encoded);
                    loaded = json.fromJson(SaveData.class, raw);
                }
            }
        } catch (Exception ex) {
            Gdx.app.error(AppConstants.LOG_TAG, "Failed to load save; will recover", ex);
        }

        loaded = migrationService.migrateIfNeeded(loaded);
        current = validator.validateAndRepair(loaded);
        save(); // write an initial clean, signed copy
    }

    /**
     * Immediately validates, encodes, signs, and flushes the current save to disk.
     * Prefer {@link #markDirty()} + {@link #flushIfDirty()} for high-frequency mutations.
     */
    public void save() {
        SaveData data = validator.validateAndRepair(get());
        String raw = json.toJson(data);
        String encoded = encode(raw);
        String sig = sign(encoded);
        try {
            prefs.putString(KEY_BACKUP_JSON, prefs.getString(KEY_SAVE_JSON, ""));
            prefs.putString(KEY_SAVE_JSON, encoded);
            prefs.putString(KEY_SAVE_SIG, sig);
            prefs.flush();
            dirty = false;
        } catch (Exception ex) {
            Gdx.app.error(AppConstants.LOG_TAG, "Failed to save preferences", ex);
        }
    }

    // -------------------------------------------------------------------------
    // Dirty-flag batching (T-2.5)
    // -------------------------------------------------------------------------

    /** Mark that in-memory state has changed; does NOT flush to disk. */
    public void markDirty() {
        dirty = true;
    }

    /** Flush to disk only if marked dirty since the last save. */
    public void flushIfDirty() {
        if (dirty) {
            save();
        }
    }

    public void reset() {
        current = validator.defaultSave();
        save();
    }

    // -------------------------------------------------------------------------
    // Encoding helpers — Base64 obfuscation (stops trivial file editing)
    // -------------------------------------------------------------------------

    private static String encode(String raw) {
        return new String(Base64Coder.encode(raw.getBytes(StandardCharsets.UTF_8)));
    }

    private static String decode(String encoded) {
        return new String(Base64Coder.decode(encoded), StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // HMAC-SHA256 integrity signature
    // -------------------------------------------------------------------------

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64Coder.encode(digest));
        } catch (Exception e) {
            Gdx.app.error(AppConstants.LOG_TAG, "HMAC signing failed", e);
            return "";
        }
    }
}

