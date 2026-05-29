# ============================================================
# Splice Lab — ProGuard / R8 rules
# ============================================================

# --- libGDX ---
-keep class com.badlogic.gdx.** { *; }
-dontwarn com.badlogic.gdx.**

# libGDX Json deserializes SaveData by reflection — every field must be kept.
-keep class com.splicelab.data.SaveData { *; }

# Keep all data/model classes used in Json serialization
-keep class com.splicelab.data.** { *; }
-keep class com.splicelab.model.** { *; }

# Keep enum names (SaveData stores enum names as strings)
-keepclassmembers enum com.splicelab.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}

# --- Android launcher ---
-keep class com.splicelab.android.AndroidLauncher { *; }

# --- Suppress common warnings ---
-dontwarn javax.annotation.**
-dontwarn org.slf4j.**
