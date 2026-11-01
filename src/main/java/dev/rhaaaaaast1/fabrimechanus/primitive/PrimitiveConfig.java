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

/** Reads the worldgen part of config/fabrimechanus/common.json. */
public final class PrimitiveConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PEBBLE_TRIES = 12;
    private static final int MAX_FLINT_TRIES = 6;

    private static volatile boolean pebbleEnabled = true;
    private static volatile int pebbleCount = 4;
    private static volatile boolean flintEnabled = true;
    private static volatile int flintCount = 1;

    private PrimitiveConfig() {
    }

    public static Path commonFile() {
        return FMLPaths.CONFIGDIR.get().resolve(MobGearConfig.DIRECTORY).resolve(MobGearConfig.MAIN_FILE);
    }

    /** Creates a minimal common.json if needed, then loads worldgen settings. */
    public static void ensureAndLoad() {
        Path path = commonFile();
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.writeString(path, """
                        {
                          "enabled": true,
                          "log_unknown_items": true,
                          "worldgen": {
                            "small_pebble": { "enabled": true, "count": 4 },
                            "small_flint": { "enabled": true, "count": 1 }
                          }
                        }
                        """);
            }
            load();
        } catch (IOException exception) {
            LOGGER.error("[fabrimechanus] failed to read worldgen config at {}", path, exception);
        }
    }

    public static void load() {
        Path path = commonFile();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path));
            if (!root.isJsonObject()) {
                return;
            }
            JsonObject worldgen = root.getAsJsonObject().getAsJsonObject("worldgen");
            if (worldgen == null) {
                return;
            }
            JsonObject pebble = worldgen.getAsJsonObject("small_pebble");
            if (pebble != null) {
                pebbleEnabled = !pebble.has("enabled") || pebble.get("enabled").getAsBoolean();
                pebbleCount = clamp(pebble.has("count") ? pebble.get("count").getAsInt() : pebbleCount, MAX_PEBBLE_TRIES);
            }
            JsonObject flint = worldgen.getAsJsonObject("small_flint");
            if (flint != null) {
                flintEnabled = !flint.has("enabled") || flint.get("enabled").getAsBoolean();
                flintCount = clamp(flint.has("count") ? flint.get("count").getAsInt() : flintCount, MAX_FLINT_TRIES);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[fabrimechanus] failed to parse worldgen config {}: {}", path, exception.getMessage());
        }
    }

    public static boolean pebbleEnabled() {
        return pebbleEnabled && pebbleCount > 0;
    }

    public static int pebbleCount() {
        return pebbleCount;
    }

    public static boolean flintEnabled() {
        return flintEnabled && flintCount > 0;
    }

    public static int flintCount() {
        return flintCount;
    }

    public static int maxPebbleTries() {
        return MAX_PEBBLE_TRIES;
    }

    public static int maxFlintTries() {
        return MAX_FLINT_TRIES;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }
}
