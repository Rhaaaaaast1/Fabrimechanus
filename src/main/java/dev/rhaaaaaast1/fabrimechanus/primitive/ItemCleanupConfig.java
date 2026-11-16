package dev.rhaaaaaast1.fabrimechanus.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.rhaaaaaast1.fabrimechanus.tinkers.MobGearConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads config/fabrimechanus/item_cleanup.json. */
public final class ItemCleanupConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile boolean enabled = true;
    private static volatile int intervalTicks = 12000;
    private static volatile int warnSeconds = 10;
    private static volatile boolean preserveNoPickup = true;
    private static volatile boolean preserveNoDespawn = true;
    private static volatile boolean showClearMessage = true;
    private static volatile boolean actionBar = true;

    private ItemCleanupConfig() {
    }

    public static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(MobGearConfig.DIRECTORY).resolve("item_cleanup.json");
    }

    public static void ensureAndLoad() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.writeString(path, """
                        {
                          "enabled": true,
                          "interval_ticks": 12000,
                          "warn_seconds": 10,
                          "preserve_no_pickup": true,
                          "preserve_no_despawn": true,
                          "show_clear_message": true,
                          "action_bar": true
                        }
                        """);
                LOGGER.info("[fabrimechanus] wrote default item cleanup config {}", path);
            }
            load();
        } catch (IOException exception) {
            LOGGER.error("[fabrimechanus] failed to create item cleanup config {}", path, exception);
        }
    }

    public static void load() {
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path));
            if (!root.isJsonObject()) {
                return;
            }
            JsonObject json = root.getAsJsonObject();
            enabled = bool(json, "enabled", enabled);
            intervalTicks = Math.max(20, integer(json, "interval_ticks", intervalTicks));
            warnSeconds = Math.max(0, Math.min(integer(json, "warn_seconds", warnSeconds), intervalTicks / 20));
            preserveNoPickup = bool(json, "preserve_no_pickup", preserveNoPickup);
            preserveNoDespawn = bool(json, "preserve_no_despawn", preserveNoDespawn);
            showClearMessage = bool(json, "show_clear_message", showClearMessage);
            actionBar = bool(json, "action_bar", actionBar);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[fabrimechanus] failed to parse item cleanup config {}: {}", path, exception.getMessage());
        }
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static int intervalTicks() {
        return intervalTicks;
    }

    public static int warnSeconds() {
        return warnSeconds;
    }

    public static boolean preserveNoPickup() {
        return preserveNoPickup;
    }

    public static boolean preserveNoDespawn() {
        return preserveNoDespawn;
    }

    public static boolean showClearMessage() {
        return showClearMessage;
    }

    public static boolean actionBar() {
        return actionBar;
    }
}
