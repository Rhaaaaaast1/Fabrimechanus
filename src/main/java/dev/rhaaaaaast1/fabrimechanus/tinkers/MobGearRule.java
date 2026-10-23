package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * One datapack entry: which mobs may spawn with which Tinkers' Construct gear.
 *
 * <p>Weights are relative within a slot: a weapon list of {@code sword(5), cleaver(2)} means the sword is rolled five
 * times out of seven. Every tool is built through {@link ToolFactory}, so each part still gets its own material roll
 * from the tier weights in the config file.</p>
 *
 * <p>Instances are produced by {@link MobGearReloadListener}; nothing in here references JSON types, so a future
 * format change only touches the listener.</p>
 */
public record MobGearRule(List<Matcher> matchers, Slot mainhand, Slot offhand, Armor armor, int priority) {
    /** Matches a mob by entity id or by entity type tag. */
    public sealed interface Matcher permits IdMatcher, TagMatcher {
        boolean matches(Mob mob);
    }

    public record IdMatcher(ResourceLocation id) implements Matcher {
        @Override
        public boolean matches(Mob mob) {
            return id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
        }
    }

    public record TagMatcher(TagKey<EntityType<?>> tag) implements Matcher {
        @Override
        public boolean matches(Mob mob) {
            return mob.getType().is(tag);
        }
    }

    /** A slot rule: roll {@code chance}, then pick one of the weighted items. */
    public record Slot(float chance, List<Weighted> items) {
        public static final Slot EMPTY = new Slot(0.0F, List.of());

        public boolean isEmpty() {
            return items.isEmpty() || chance <= 0.0F;
        }

        /** Builds one weighted tool, or an empty stack when this slot has no candidates. */
        public ItemStack roll(RandomSource random, RegistryAccess access) {
            if (items.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int total = 0;
            for (Weighted weighted : items) {
                total += Math.max(1, weighted.weight());
            }
            int roll = random.nextInt(total);
            for (Weighted weighted : items) {
                roll -= Math.max(1, weighted.weight());
                if (roll < 0) {
                    return ToolFactory.build(weighted.item(), random, access);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    public record Weighted(Item item, int weight) {
    }

    /**
     * Which armor set a mob may spawn with, plus the chance for the whole set.
     *
     * <p>Several sets can be listed with weights, in which case one set is rolled per mob and the whole set uses it, so
     * a mob never ends up with a mixed plate/travelers outfit.</p>
     */
    public record Armor(float chance, List<ArmorChoice> choices) {
        public static final Armor NONE = new Armor(0.0F, List.of());

        public boolean isEmpty() {
            return chance <= 0.0F || choices.isEmpty();
        }

        /** Picks the set for this mob, or {@link ArmorSet#NONE} when this rule adds no armor. */
        public ArmorSet roll(RandomSource random) {
            if (choices.isEmpty()) {
                return ArmorSet.NONE;
            }
            int total = 0;
            for (ArmorChoice choice : choices) {
                total += Math.max(1, choice.weight());
            }
            int roll = random.nextInt(total);
            for (ArmorChoice choice : choices) {
                roll -= Math.max(1, choice.weight());
                if (roll < 0) {
                    return choice.set();
                }
            }
            return choices.get(0).set();
        }

        /** Resolves the armor item for one slot, or null when the set has no piece for it. */
        @Nullable
        public static Item item(ArmorSet set, ArmorItem.Type type) {
            return switch (set) {
                case PLATE -> TinkerTools.plateArmor.get(type);
                case TRAVELERS -> TinkerTools.travelersGear.get(type);
                case NONE -> null;
            };
        }
    }

    /** One possible armor set with its relative weight. */
    public record ArmorChoice(ArmorSet set, int weight) {
    }

    public enum ArmorSet {
        NONE,
        PLATE,
        TRAVELERS
    }

    public boolean matches(Mob mob) {
        for (Matcher matcher : matchers) {
            if (matcher.matches(mob)) {
                return true;
            }
        }
        return false;
    }

    /** True if this rule hands out any weapon at all; used to skip the main hand roll entirely. */
    public boolean hasWeapon() {
        return !mainhand.isEmpty();
    }

    public static TagKey<EntityType<?>> entityTag(ResourceLocation id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }
}
