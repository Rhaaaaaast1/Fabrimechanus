package dev.rhaaaaaast1.fabrimechanus.tinkers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Loads mob gear rules from {@code config/fabrimechanus/mob_gear/}.
 *
 * <p>The folder is created on first launch with one JSON file per vanilla mob. Packs can edit those files or add
 * their own under {@code minecraft/} or {@code compat/}. The same reload listener mechanism is kept, so {@code /reload}
 * re-reads the folder.</p>
 *
 * <p>File format is the same as the old datapack format; the only difference is the location. Unknown items are
 * skipped with a log line instead of breaking the whole reload.</p>
 */
public final class MobGearConfig implements PreparableReloadListener {
    /** Folder inside the instance's config directory. */
    public static final String DIRECTORY = "fabrimechanus";
    public static final String VANILLA = "minecraft";
    public static final String COMPAT = "compat";
    public static final String MAIN_FILE = "common.json";

    private static final String DEFAULTS_RESOURCE = "/fabrimechanus/mob_gear_defaults/minecraft/";
    private static final String DEFAULT_MAIN = """
            {
              "enabled": true,
              "log_unknown_items": true
            }
            """;
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Default files copied into a fresh config folder. Keep in sync with resources/fabrimechanus/mob_gear_defaults. */
    private static final List<String> DEFAULT_MOBS = List.of(
            "bogged", "drowned", "husk", "illusioner", "piglin", "piglin_brute", "pillager",
            "skeleton", "stray", "vex", "vindicator", "wither_skeleton", "zombie",
            "zombie_villager", "zombified_piglin"
    );

    public static Path configRoot() {
        return FMLPaths.CONFIGDIR.get().resolve(DIRECTORY);
    }

    /**
     * Creates the config folder and copies any missing default files. Existing files are never overwritten, so edits
     * survive updates.
     */
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

    /** Re-reads the config folder and replaces the active rule list. */
    public static void reloadNow() {
        ensureDefaults();
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

            JsonElement json;
            try {
                json = JsonParser.parseString(Files.readString(file));
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("[fabrimechanus] skipping unreadable mob gear file {}: {}", relative, exception.getMessage());
                continue;
            }

            DataResult<MobGearJson> parsed = MobGearJson.CODEC.parse(JsonOps.INSTANCE, json);
            MobGearJson data = parsed.resultOrPartial(error -> LOGGER.warn("[fabrimechanus] skipping mob gear file {}: {}", relative, error)).orElse(null);
            if (data == null) {
                continue;
            }
            try {
                MobGearRule rule = data.toRule(relative);
                if (rule != null) {
                    rules.add(rule);
                }
            } catch (RuntimeException exception) {
                // one broken file must never take down the whole reload, other files still need their rules
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
            var object = json.getAsJsonObject();
            boolean enabled = !object.has("enabled") || object.get("enabled").getAsBoolean();
            boolean logUnknown = !object.has("log_unknown_items") || object.get("log_unknown_items").getAsBoolean();
            return new MainOptions(enabled, logUnknown);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[fabrimechanus] failed to read {}, using defaults: {}", path, exception.getMessage());
            return new MainOptions(true, true);
        }
    }

    private record MainOptions(boolean enabled, boolean logUnknownItems) {
    }

    /** Raw JSON shape of one file. Kept separate from {@link MobGearRule} so the format can evolve on its own. */
    record MobGearJson(List<String> mobs, Optional<SlotJson> mainhand, Optional<SlotJson> offhand,
                       Optional<ArmorJson> armor, int priority, int materialTier) {
        static final Codec<MobGearJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("mobs").forGetter(MobGearJson::mobs),
                SlotJson.CODEC.optionalFieldOf("mainhand").forGetter(MobGearJson::mainhand),
                SlotJson.CODEC.optionalFieldOf("offhand").forGetter(MobGearJson::offhand),
                ArmorJson.CODEC.optionalFieldOf("armor").forGetter(MobGearJson::armor),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(MobGearJson::priority),
                Codec.INT.optionalFieldOf("materialTier", 0).forGetter(MobGearJson::materialTier)
        ).apply(instance, MobGearJson::new));

        @Nullable
        MobGearRule toRule(String fileLabel) {
            List<MobGearRule.Matcher> matchers = new ArrayList<>();
            for (String raw : this.mobs) {
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
            return new MobGearRule(
                    List.copyOf(matchers),
                    mainhand.map(SlotJson::toSlot).orElse(MobGearRule.Slot.EMPTY),
                    offhand.map(SlotJson::toSlot).orElse(MobGearRule.Slot.EMPTY),
                    armor.map(ArmorJson::toArmor).orElse(MobGearRule.Armor.NONE),
                    priority,
                    materialTier
            );
        }
    }

    record SlotJson(float chance, List<ItemJson> items) {
        static final Codec<SlotJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(SlotJson::chance),
                ItemJson.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(SlotJson::items)
        ).apply(instance, SlotJson::new));

        MobGearRule.Slot toSlot() {
            List<MobGearRule.Weighted> resolved = new ArrayList<>();
            for (ItemJson json : this.items) {
                Item item = BuiltInRegistries.ITEM.get(json.item());
                if (item == Items.AIR && !BuiltInRegistries.ITEM.containsKey(json.item())) {
                    LOGGER.warn("[fabrimechanus] unknown item '{}' in mob gear config, skipping it", json.item());
                    continue;
                }
                resolved.add(new MobGearRule.Weighted(item, json.weight()));
            }
            return new MobGearRule.Slot(chance, List.copyOf(resolved));
        }
    }

    record ItemJson(ResourceLocation item, int weight) {
        static final Codec<ItemJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(ItemJson::item),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(ItemJson::weight)
        ).apply(instance, ItemJson::new));
    }

    /** Accepts both {@code "set": "plate"} and {@code "sets": [{"set": "plate", "weight": 3}, ...]}. */
    record ArmorJson(float chance, Optional<String> set, List<SetJson> sets) {
        static final Codec<ArmorJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("chance", 0.0F).forGetter(ArmorJson::chance),
                Codec.STRING.optionalFieldOf("set").forGetter(ArmorJson::set),
                SetJson.CODEC.listOf().optionalFieldOf("sets", List.of()).forGetter(ArmorJson::sets)
        ).apply(instance, ArmorJson::new));

        MobGearRule.Armor toArmor() {
            List<MobGearRule.ArmorChoice> choices = new ArrayList<>();
            for (SetJson json : this.sets) {
                MobGearRule.ArmorSet set = parseSet(json.set());
                if (set != MobGearRule.ArmorSet.NONE) {
                    choices.add(new MobGearRule.ArmorChoice(set, json.weight()));
                }
            }
            if (choices.isEmpty() && this.set.isPresent()) {
                MobGearRule.ArmorSet set = parseSet(this.set.get());
                if (set != MobGearRule.ArmorSet.NONE) {
                    choices.add(new MobGearRule.ArmorChoice(set, 1));
                }
            }
            return choices.isEmpty() ? MobGearRule.Armor.NONE : new MobGearRule.Armor(chance, List.copyOf(choices));
        }
    }

    record SetJson(String set, int weight) {
        static final Codec<SetJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("set").forGetter(SetJson::set),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(SetJson::weight)
        ).apply(instance, SetJson::new));
    }

    /** Parses an armor set name, warning and falling back to {@code none} for unknown values. */
    private static MobGearRule.ArmorSet parseSet(String name) {
        try {
            return MobGearRule.ArmorSet.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("[fabrimechanus] unknown armor set '{}' in mob gear config, expected plate/travelers/none", name);
            return MobGearRule.ArmorSet.NONE;
        }
    }
}
