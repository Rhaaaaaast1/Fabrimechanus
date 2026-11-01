package dev.rhaaaaaast1.fabrimechanus.tinkers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.rhaaaaaast1.fabrimechanus.primitive.PrimitiveConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Loads JSON mob gear rules from {@code config/fabrimechanus/}.
 *
 * <p>The folder contains common.json, minecraft/*.json and an empty compat/ folder. Vanilla tools and armor are
 * stripped from spawning monsters before these rules build their Tinkers' replacements.</p>
 */
public final class MobGearConfig implements PreparableReloadListener {
    public static final String DIRECTORY = "fabrimechanus";
    public static final String VANILLA = "minecraft";
    public static final String COMPAT = "compat";
    public static final String MAIN_FILE = "common.json";

    private static final String DEFAULTS_RESOURCE = "/fabrimechanus/mob_gear_defaults/minecraft/";
    private static final String DEFAULT_MAIN = """
            {
              "enabled": true,
              "log_unknown_items": true,
              "worldgen": {
                "small_pebble": { "enabled": true, "count": 4 },
                "small_flint": { "enabled": true, "count": 1 }
              }
            }
            """;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> ANCIENT_TOOLS = List.of(
            "tconstruct:melting_pan", "tconstruct:war_pick", "tconstruct:battlesign",
            "tconstruct:swasher", "tconstruct:minotaur_axe"
    );
    private static final List<String> WIDE_TOOLS = List.of(
            "tconstruct:cleaver", "tconstruct:broad_axe", "tconstruct:scythe"
    );

    private static final List<String> DEFAULT_MOBS = List.of(
            "bogged", "drowned", "husk", "illusioner", "piglin", "piglin_brute", "pillager",
            "skeleton", "stray", "vex", "vindicator", "wither_skeleton", "zombie",
            "zombie_villager", "zombified_piglin"
    );

    public static Path configRoot() {
        return FMLPaths.CONFIGDIR.get().resolve(DIRECTORY);
    }

    public static void ensureDefaults() {
        Path root = configRoot();
        try {
            Files.createDirectories(root.resolve(VANILLA));
            Files.createDirectories(root.resolve(COMPAT));
            for (String mob : DEFAULT_MOBS) {
                Path target = root.resolve(VANILLA).resolve(mob + ".json");
                if (!Files.exists(target)) {
                    copyDefault(mob, target);
                }
            }
            Path main = root.resolve(MAIN_FILE);
            if (!Files.exists(main)) {
                Files.writeString(main, DEFAULT_MAIN);
            }
        } catch (IOException exception) {
            LOGGER.error("[fabrimechanus] failed to create mob gear config at {}", root, exception);
        }
    }

    private static void copyDefault(String mob, Path target) {
        String resource = DEFAULTS_RESOURCE + mob + ".json";
        try (InputStream stream = MobGearConfig.class.getResourceAsStream(resource)) {
            if (stream == null) {
                LOGGER.warn("[fabrimechanus] missing bundled default mob gear file {}", resource);
                return;
            }
            Files.copy(stream, target);
            LOGGER.info("[fabrimechanus] wrote default mob gear config {}", target);
        } catch (IOException exception) {
            LOGGER.error("[fabrimechanus] failed to write default mob gear config {}", target, exception);
        }
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                         ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                         Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.runAsync(MobGearConfig::reloadNow, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenRunAsync(() -> {
                }, gameExecutor);
    }

    public static void reloadNow() {
        ensureDefaults();
        PrimitiveConfig.load();
        Path root = configRoot();
        List<MobGearRule> rules = new ArrayList<>();
        int vanilla = 0;
        int compat = 0;
        int other = 0;

        if (!Files.isDirectory(root)) {
            MobGearRegistry.reload(List.of());
            LOGGER.warn("[fabrimechanus] mob gear config folder {} does not exist", root);
            return;
        }

        MainOptions options = readMain(root.resolve(MAIN_FILE));
        if (!options.enabled()) {
            MobGearRegistry.reload(List.of());
            LOGGER.info("[fabrimechanus] mob gear disabled in {}", root.resolve(MAIN_FILE));
            return;
        }

        List<Path> files;
        try (Stream<Path> stream = Files.walk(root)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals(MAIN_FILE))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException exception) {
            MobGearRegistry.reload(List.of());
            LOGGER.error("[fabrimechanus] failed to list mob gear config {}", root, exception);
            return;
        }

        for (Path file : files) {
            String relative = root.relativize(file).toString().replace('\\', '/');
            if (relative.startsWith(VANILLA + "/")) {
                vanilla++;
            } else if (relative.startsWith(COMPAT + "/")) {
                compat++;
            } else {
                other++;
                LOGGER.warn("[fabrimechanus] mob gear file {} is outside '{}/' and '{}/', it works but should be moved into one of them",
                        relative, VANILLA, COMPAT);
            }

            JsonObject json;
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(file));
                if (!parsed.isJsonObject()) {
                    LOGGER.warn("[fabrimechanus] skipping mob gear file {}: root is not an object", relative);
                    continue;
                }
                json = parsed.getAsJsonObject();
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("[fabrimechanus] skipping unreadable mob gear file {}: {}", relative, exception.getMessage());
                continue;
            }

            try {
                MobGearRule rule = parseRule(json, relative, options);
                if (rule != null) {
                    rules.add(rule);
                }
            } catch (RuntimeException exception) {
                LOGGER.error("[fabrimechanus] failed to build mob gear rule from {}", relative, exception);
            }
        }

        MobGearRegistry.reload(rules);
        LOGGER.info("[fabrimechanus] loaded {} mob gear rules from config ({}: {}, {}: {}, other: {})",
                rules.size(), VANILLA, vanilla, COMPAT, compat, other);
    }

    private static MainOptions readMain(Path path) {
        if (!Files.isRegularFile(path)) {
            return new MainOptions(true, true);
        }
        try {
            JsonElement json = JsonParser.parseString(Files.readString(path));
            if (!json.isJsonObject()) {
                return new MainOptions(true, true);
            }
            JsonObject object = json.getAsJsonObject();
            boolean enabled = !object.has("enabled") || object.get("enabled").getAsBoolean();
            boolean logUnknown = !object.has("log_unknown_items") || object.get("log_unknown_items").getAsBoolean();
            return new MainOptions(enabled, logUnknown);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[fabrimechanus] failed to read {}, using defaults: {}", path, exception.getMessage());
            return new MainOptions(true, true);
        }
    }

    @Nullable
    private static MobGearRule parseRule(JsonObject json, String fileLabel, MainOptions options) {
        List<String> mobs = stringList(json.getAsJsonArray("mobs"));
        if (mobs.isEmpty()) {
            LOGGER.warn("[fabrimechanus] mob gear file {} has no usable mobs, skipping it", fileLabel);
            return null;
        }

        List<MobGearRule.Matcher> matchers = new ArrayList<>();
        for (String raw : mobs) {
            if (raw.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(raw.substring(1));
                if (tagId == null) {
                    LOGGER.warn("[fabrimechanus] invalid entity tag '{}' in mob gear file {}", raw, fileLabel);
                } else {
                    matchers.add(new MobGearRule.TagMatcher(MobGearRule.entityTag(tagId)));
                }
            } else {
                ResourceLocation mobId = ResourceLocation.tryParse(raw);
                if (mobId == null) {
                    LOGGER.warn("[fabrimechanus] invalid entity id '{}' in mob gear file {}", raw, fileLabel);
                } else {
                    matchers.add(new MobGearRule.IdMatcher(mobId));
                }
            }
        }
        if (matchers.isEmpty()) {
            LOGGER.warn("[fabrimechanus] mob gear file {} has no usable mobs, skipping it", fileLabel);
            return null;
        }

        int priority = intOr(json, "priority", 0);
        MobGearRule.Slot mainhand = parseSlot(json.getAsJsonObject("mainhand"), fileLabel, options);
        MobGearRule.Armor armor = parseArmor(json.getAsJsonObject("armor"), fileLabel);
        return new MobGearRule(List.copyOf(matchers), mainhand, armor, priority);
    }

    private static MobGearRule.Slot parseSlot(@Nullable JsonObject json, String fileLabel, MainOptions options) {
        if (json == null) {
            return MobGearRule.Slot.EMPTY;
        }
        MaterialWeights materials = parseWeights(json.getAsJsonObject("materials"), MaterialWeights.DEFAULT);
        List<MobGearRule.Weighted> items = new ArrayList<>();
        Set<ResourceLocation> seen = new HashSet<>();
        JsonElement itemsElement = json.get("items");
        if (itemsElement != null && itemsElement.isJsonArray()) {
            for (JsonElement element : itemsElement.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject itemJson = element.getAsJsonObject();
                ResourceLocation id = ResourceLocation.tryParse(stringOr(itemJson, "item", ""));
                if (id == null) {
                    LOGGER.warn("[fabrimechanus] invalid item id in {}", fileLabel);
                    continue;
                }
                String idText = id.toString();
                if (ANCIENT_TOOLS.contains(idText)) {
                    continue;
                }
                boolean enabled = itemJson.has("enabled")
                        ? itemJson.get("enabled").getAsBoolean()
                        : !WIDE_TOOLS.contains(idText);
                if (!enabled) {
                    continue;
                }
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR && !BuiltInRegistries.ITEM.containsKey(id)) {
                    if (options.logUnknownItems()) {
                        LOGGER.warn("[fabrimechanus] unknown item '{}' in {}, skipping it", id, fileLabel);
                    }
                    continue;
                }
                if (!seen.add(id)) {
                    LOGGER.warn("[fabrimechanus] duplicate item '{}' in {}, keeping the first enabled entry", id, fileLabel);
                    continue;
                }
                items.add(new MobGearRule.Weighted(item, intOr(itemJson, "weight", 1)));
            }
        }
        return new MobGearRule.Slot(List.copyOf(items), materials);
    }

    private static MobGearRule.Armor parseArmor(@Nullable JsonObject json, String fileLabel) {
        if (json == null) {
            return MobGearRule.Armor.NONE;
        }
        List<MobGearRule.ArmorChoice> choices = new ArrayList<>();
        JsonElement sets = json.get("sets");
        if (sets != null && sets.isJsonArray()) {
            for (JsonElement element : sets.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject setJson = element.getAsJsonObject();
                MobGearRule.ArmorSet set = parseSet(stringOr(setJson, "set", ""));
                if (set != MobGearRule.ArmorSet.NONE) {
                    choices.add(new MobGearRule.ArmorChoice(set, intOr(setJson, "weight", 1)));
                }
            }
        }
        if (choices.isEmpty() && json.has("set")) {
            MobGearRule.ArmorSet set = parseSet(json.get("set").getAsString());
            if (set != MobGearRule.ArmorSet.NONE) {
                choices.add(new MobGearRule.ArmorChoice(set, 1));
            }
        }

        MaterialWeights materials = parseWeights(json.getAsJsonObject("materials"), MaterialWeights.DEFAULT);
        return choices.isEmpty() ? MobGearRule.Armor.NONE : new MobGearRule.Armor(List.copyOf(choices), materials);
    }

    private static MaterialWeights parseWeights(@Nullable JsonObject json, MaterialWeights fallback) {
        if (json == null) {
            return fallback;
        }
        return new MaterialWeights(
                intOr(json, "tier1", fallback.tier1()),
                intOr(json, "tier2", fallback.tier2()),
                intOr(json, "tier3", fallback.tier3()),
                intOr(json, "tier4", fallback.tier4())
        );
    }

    private static List<String> stringList(@Nullable JsonArray array) {
        if (array == null) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    private static int intOr(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static String stringOr(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static MobGearRule.ArmorSet parseSet(String name) {
        try {
            return MobGearRule.ArmorSet.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("[fabrimechanus] unknown armor set '{}' in mob gear config, expected plate/travelers/none", name);
            return MobGearRule.ArmorSet.NONE;
        }
    }

    private record MainOptions(boolean enabled, boolean logUnknownItems) {
    }
}
