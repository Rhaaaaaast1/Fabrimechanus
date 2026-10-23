package dev.rhaaaaaast1.fabrimechanus.tinkers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the mob gear rules from {@code data/<namespace>/fabrimechanus/mob_gear/**}{@code /*.json}.
 *
 * <p>The folder is split by the mod the mobs come from: vanilla mobs live in {@code mob_gear/minecraft/}, mobs added by
 * other mods go into {@code mob_gear/compat/} (usually into {@code mob_gear/compat/<modid>/}). The paths are only
 * organisational, but rules outside those two folders are reported, since they are almost always a mistake.</p>
 *
 * <p>The listener is what makes the feature pack friendly: any mod or pack can add rules for its own mobs by dropping a
 * JSON file into that folder, and can override a shipped rule by using a higher {@code priority}. The file format is
 * documented in the README; unknown items are skipped with a log line instead of breaking the whole reload.</p>
 */
public final class MobGearReloadListener extends SimpleJsonResourceReloadListener {
    /** Folder name inside a datapack's {@code data/<namespace>/} directory. */
    public static final String DIRECTORY = "fabrimechanus/mob_gear";
    /** Sub folder holding rules for vanilla mobs. */
    public static final String VANILLA = "minecraft";
    /** Sub folder holding rules for mobs added by other mods. */
    public static final String COMPAT = "compat";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public MobGearReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<MobGearRule> rules = new ArrayList<>();
        int vanilla = 0;
        int compat = 0;
        int other = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
            String path = entry.getKey().getPath();
            if (path.startsWith(VANILLA + "/")) {
                vanilla++;
            } else if (path.startsWith(COMPAT + "/")) {
                compat++;
            } else {
                other++;
                LOGGER.warn("Mob gear file {} is outside '{}/' and '{}/', it works but should be moved into one of them",
                        entry.getKey(), VANILLA, COMPAT);
            }
            DataResult<MobGearJson> parsed = MobGearJson.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            MobGearJson json = parsed.resultOrPartial(error -> LOGGER.warn("Skipping mob gear file {}: {}", entry.getKey(), error)).orElse(null);
            if (json != null) {
                MobGearRule rule = json.toRule(entry.getKey());
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }
        MobGearRegistry.reload(rules);
        LOGGER.info("Loaded {} mob gear rules from datapacks ({}: {}, {}: {}, other: {})", rules.size(),
                VANILLA, vanilla, COMPAT, compat, other);
    }

    /** Raw JSON shape of one file. Kept separate from {@link MobGearRule} so the format can evolve on its own. */
    record MobGearJson(List<String> mobs, Optional<SlotJson> mainhand, Optional<SlotJson> offhand,
                       Optional<ArmorJson> armor, int priority) {
        static final Codec<MobGearJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("mobs").forGetter(MobGearJson::mobs),
                SlotJson.CODEC.optionalFieldOf("mainhand").forGetter(MobGearJson::mainhand),
                SlotJson.CODEC.optionalFieldOf("offhand").forGetter(MobGearJson::offhand),
                ArmorJson.CODEC.optionalFieldOf("armor").forGetter(MobGearJson::armor),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(MobGearJson::priority)
        ).apply(instance, MobGearJson::new));

        @Nullable
        MobGearRule toRule(ResourceLocation fileId) {
            List<MobGearRule.Matcher> matchers = new ArrayList<>();
            for (String raw : this.mobs) {
                if (raw.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(raw.substring(1));
                    if (tagId == null) {
                        LOGGER.warn("Invalid entity tag '{}' in mob gear file {}", raw, fileId);
                    } else {
                        matchers.add(new MobGearRule.TagMatcher(MobGearRule.entityTag(tagId)));
                    }
                } else {
                    ResourceLocation mobId = ResourceLocation.tryParse(raw);
                    if (mobId == null) {
                        LOGGER.warn("Invalid entity id '{}' in mob gear file {}", raw, fileId);
                    } else {
                        matchers.add(new MobGearRule.IdMatcher(mobId));
                    }
                }
            }
            if (matchers.isEmpty()) {
                LOGGER.warn("Mob gear file {} has no usable mobs, skipping it", fileId);
                return null;
            }
            return new MobGearRule(
                    List.copyOf(matchers),
                    mainhand.map(SlotJson::toSlot).orElse(MobGearRule.Slot.EMPTY),
                    offhand.map(SlotJson::toSlot).orElse(MobGearRule.Slot.EMPTY),
                    armor.map(ArmorJson::toArmor).orElse(MobGearRule.Armor.NONE),
                    priority
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
                    LOGGER.warn("Unknown item '{}' in mob gear data, skipping it", json.item());
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
            LOGGER.warn("Unknown armor set '{}' in mob gear data, expected plate/travelers/none", name);
            return MobGearRule.ArmorSet.NONE;
        }
    }
}