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
 * Loads the mob gear rules from {@code data/<namespace>/fabrimechanus/mob_gear/*.json}.
 *
 * <p>The listener is what makes the feature pack friendly: any mod or pack can add rules for its own mobs by dropping a
 * JSON file into that folder, and can override a shipped rule by using a higher {@code priority}. The file format is
 * documented in the README; unknown items are skipped with a log line instead of breaking the whole reload.</p>
 */
public final class MobGearReloadListener extends SimpleJsonResourceReloadListener {
    /** Folder name inside a datapack's {@code data/<namespace>/} directory. */
    public static final String DIRECTORY = "fabrimechanus/mob_gear";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public MobGearReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<MobGearRule> rules = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
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
        LOGGER.info("Loaded {} mob gear rules from datapacks", rules.size());
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

    record ArmorJson(float chance, String set) {
        static final Codec<ArmorJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("chance", 0.0F).forGetter(ArmorJson::chance),
                Codec.STRING.optionalFieldOf("set", "none").forGetter(ArmorJson::set)
        ).apply(instance, ArmorJson::new));

        MobGearRule.Armor toArmor() {
            MobGearRule.ArmorSet armorSet;
            try {
                armorSet = MobGearRule.ArmorSet.valueOf(set.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                LOGGER.warn("Unknown armor set '{}' in mob gear data, expected plate/travelers/none", set);
                armorSet = MobGearRule.ArmorSet.NONE;
            }
            return new MobGearRule.Armor(chance, armorSet);
        }
    }
}
