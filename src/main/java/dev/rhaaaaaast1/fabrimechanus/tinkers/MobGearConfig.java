package dev.rhaaaaaast1.fabrimechanus.tinkers;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
 * <p>Layout:</p>
 * <pre>
 * config/fabrimechanus/mob_gear/
 *   common.toml          global options
 *   minecraft/*.toml     one rule file per vanilla mob
 *   compat/              rules for modded mobs (empty by default)
 * </pre>
 *
 * <p>Existing files are never overwritten. The folder is re-read by {@code /reload}.</p>
 */
public final class MobGearConfig implements PreparableReloadListener {
    public static final String DIRECTORY = "fabrimechanus/mob_gear";
    public static final String VANILLA = "minecraft";
    public static final String COMPAT = "compat";
    public static final String MAIN_FILE = "common.toml";

    private static final String DEFAULTS_RESOURCE = "/fabrimechanus/mob_gear_defaults/minecraft/";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<String> DEFAULT_MOBS = List.of(
            "bogged", "drowned", "husk", "illusioner", "piglin", "piglin_brute", "pillager",
            "skeleton", "stray", "vex", "vindicator", "wither_skeleton", "zombie",
            "zombie_villager", "zombified_piglin"
    );

    private static final String DEFAULT_MAIN = """
            # Fabrimechanus mob gear
            # 修改后使用 /reload 重新读取

            enabled = true
            log_unknown_items = true
            """;

    public static Path configRoot() {
        return FMLPaths.CONFIGDIR.get().resolve(DIRECTORY);
    }

    /** Creates the folder, common.toml, minecraft/*.toml defaults and an empty compat/. */
    public static void ensureDefaults() {
        Path root = configRoot();
        try {
            Files.createDirectories(root.resolve(VANILLA));
            Files.createDirectories(root.resolve(COMPAT));

            Path main = root.resolve(MAIN_FILE);
            if (!Files.exists(main)) {
                Files.writeString(main, DEFAULT_MAIN, StandardCharsets.UTF_8);
                LOGGER.info("[fabrimechanus] wrote default mob gear config {}", main);
            }

            for (String mob : DEFAULT_MOBS) {
                Path target = root.resolve(VANILLA).resolve(mob + ".toml");
                if (!Files.exists(target)) {
                    copyDefault(mob, target);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("[fabrimechanus] failed to create mob gear config at {}", root, exception);
        }
    }

    private static void copyDefault(String mob, Path target) {
        String resource = DEFAULTS_RESOURCE + mob + ".toml";
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
                    .filter(path -> path.getFileName().toString().endsWith(".toml"))
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

            CommentedConfig config;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                config = new TomlParser().parse(reader);
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("[fabrimechanus] skipping unreadable mob gear file {}: {}", relative, exception.getMessage());
                continue;
            }

            try {
                MobGearRule rule = readRule(config, relative, options);
                if (rule != null) {
                    rules.add(rule);
                }
            } catch (RuntimeException exception) {
                LOGGER.error("[fabrimechanus] failed to build mob gear rule from {}", relative, exception);
            }
        }

        MobGearRegistry.reload(rules);
        LOGGER.info("[fabrimechanus] loaded {} mob gear rules from TOML ({}: {}, {}: {}, other: {})",
                rules.size(), VANILLA, vanilla, COMPAT, compat, other);
    }

    private static MainOptions readMain(Path path) {
        if (!Files.isRegularFile(path)) {
            return new MainOptions(true, true);
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            CommentedConfig config = new TomlParser().parse(reader);
            return new MainOptions(
                    config.getOrElse("enabled", Boolean.TRUE),
                    config.getOrElse("log_unknown_items", Boolean.TRUE)
            );
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[fabrimechanus] failed to read {}, using defaults: {}", path, exception.getMessage());
            return new MainOptions(true, true);
        }
    }

    @Nullable
    private static MobGearRule readRule(CommentedConfig config, String fileLabel, MainOptions options) {
        List<String> rawMobs = stringList(config.get("mobs"));
        if (rawMobs.isEmpty()) {
            LOGGER.warn("[fabrimechanus] mob gear file {} has no usable mobs, skipping it", fileLabel);
            return null;
        }

        List<MobGearRule.Matcher> matchers = new ArrayList<>();
        for (String raw : rawMobs) {
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

        int priority = config.getIntOrElse("priority", 0);
        MobGearRule.Slot mainhand = readSlot(config.get("mainhand"), fileLabel, options);
        MobGearRule.Slot offhand = readSlot(config.get("offhand"), fileLabel, options);
        MobGearRule.Armor armor = readArmor(config.get("armor"), fileLabel);
        return new MobGearRule(List.copyOf(matchers), mainhand, offhand, armor, priority);
    }

    private static MobGearRule.Slot readSlot(Object value, String fileLabel, MainOptions options) {
        if (!(value instanceof UnmodifiableConfig config)) {
            return MobGearRule.Slot.EMPTY;
        }
        float chance = ((Number) config.getOrElse("chance", 1.0D)).floatValue();
        List<MobGearRule.Weighted> items = new ArrayList<>();
        for (Object raw : objectList(config.get("items"))) {
            if (!(raw instanceof UnmodifiableConfig item)) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(String.valueOf((Object) item.get("item")));
            if (id == null) {
                LOGGER.warn("[fabrimechanus] invalid item id in {}", fileLabel);
                continue;
            }
            Item resolved = BuiltInRegistries.ITEM.get(id);
            if (resolved == Items.AIR && !BuiltInRegistries.ITEM.containsKey(id)) {
                if (options.logUnknownItems()) {
                    LOGGER.warn("[fabrimechanus] unknown item '{}' in {}, skipping it", id, fileLabel);
                }
                continue;
            }
            items.add(new MobGearRule.Weighted(resolved, item.getIntOrElse("weight", 1)));
        }
        return new MobGearRule.Slot(chance, List.copyOf(items));
    }

    private static MobGearRule.Armor readArmor(Object value, String fileLabel) {
        if (!(value instanceof UnmodifiableConfig config)) {
            return MobGearRule.Armor.NONE;
        }
        float chance = ((Number) config.getOrElse("chance", 0.0D)).floatValue();
        List<MobGearRule.ArmorChoice> choices = new ArrayList<>();
        for (Object raw : objectList(config.get("sets"))) {
            if (!(raw instanceof UnmodifiableConfig setConfig)) {
                continue;
            }
            MobGearRule.ArmorSet set = parseSet(String.valueOf((Object) setConfig.get("set")));
            if (set != MobGearRule.ArmorSet.NONE) {
                choices.add(new MobGearRule.ArmorChoice(set, setConfig.getIntOrElse("weight", 1)));
            }
        }
        Optional<String> single = Optional.ofNullable(config.get("set")).map(String::valueOf);
        if (choices.isEmpty() && single.isPresent()) {
            MobGearRule.ArmorSet set = parseSet(single.get());
            if (set != MobGearRule.ArmorSet.NONE) {
                choices.add(new MobGearRule.ArmorChoice(set, 1));
            }
        }
        return choices.isEmpty() ? MobGearRule.Armor.NONE : new MobGearRule.Armor(chance, List.copyOf(choices));
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        for (Object raw : objectList(value)) {
            result.add(String.valueOf(raw));
        }
        return result;
    }

    private static List<?> objectList(Object value) {
        return value instanceof List<?> list ? list : List.of();
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
