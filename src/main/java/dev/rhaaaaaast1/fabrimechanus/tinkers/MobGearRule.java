package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.List;

/**
 * One mob gear rule. Vanilla gear is stripped first; this rule only builds the replacement Tinkers' gear.
 */
public record MobGearRule(List<Matcher> matchers, Slot mainhand, Armor armor, int priority) {
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

    /** Weighted Tinkers' tools and their material tier weights. */
    public record Slot(List<Weighted> items, MaterialWeights materials) {
        public static final Slot EMPTY = new Slot(List.of(), MaterialWeights.DEFAULT);

        public boolean isEmpty() {
            return items.isEmpty();
        }

        /** Picks one enabled item and builds it with a tier rolled from this slot's weights. */
        public ItemStack roll(RandomSource random, RegistryAccess access) {
            if (items.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int total = 0;
            for (Weighted weighted : items) {
                total = addWeight(total, weighted.weight());
            }
            int roll = random.nextInt(total);
            for (Weighted weighted : items) {
                roll -= clampWeight(weighted.weight());
                if (roll < 0) {
                    return ToolFactory.build(weighted.item(), random, access, materials);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    public record Weighted(Item item, int weight) {
    }

    /** Armor set choice plus one material tier roll shared by the whole set. */
    public record Armor(List<ArmorChoice> choices, MaterialWeights materials) {
        public static final Armor NONE = new Armor(List.of(), MaterialWeights.DEFAULT);

        public boolean isEmpty() {
            return choices.isEmpty();
        }

        public ArmorSet roll(RandomSource random) {
            if (choices.isEmpty()) {
                return ArmorSet.NONE;
            }
            int total = 0;
            for (ArmorChoice choice : choices) {
                total = addWeight(total, choice.weight());
            }
            int roll = random.nextInt(total);
            for (ArmorChoice choice : choices) {
                roll -= clampWeight(choice.weight());
                if (roll < 0) {
                    return choice.set();
                }
            }
            return choices.get(0).set();
        }

        @Nullable
        public static Item item(ArmorSet set, ArmorItem.Type type) {
            return switch (set) {
                case PLATE -> TinkerTools.plateArmor.get(type);
                case TRAVELERS -> TinkerTools.travelersGear.get(type);
                case NONE -> null;
            };
        }
    }

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

    public static TagKey<EntityType<?>> entityTag(ResourceLocation id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }

    private static final int MAX_WEIGHT = 100_000;

    private static int clampWeight(int weight) {
        return Mth.clamp(weight, 1, MAX_WEIGHT);
    }

    private static int addWeight(int total, int weight) {
        return (int) Math.min(Integer.MAX_VALUE, (long) total + clampWeight(weight));
    }
}
