package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Describes what Tinkers' Construct gear a single mob type may spawn with.
 *
 * <p>Weights are relative within a slot: a weapon list of {@code sword(5), cleaver(2)} means the sword is rolled 5 out
 * of 7 times. Every tool is built through {@link ToolFactory}, so each part gets its own material roll.</p>
 */
public final class MobGearPlan {
    /** Which Tinkers' armor set a mob may spawn with. */
    public enum ArmorSet {
        NONE,
        PLATE,
        TRAVELERS
    }

    /** Ranged behavior a mob gets in addition to its gear. */
    public enum Ranged {
        NONE,
        BOW,
        CROSSBOW
    }

    public record WeightedItem(Supplier<? extends Item> item, int weight) {
    }

    private final List<WeightedItem> weapons;
    private final List<WeightedItem> offhands;
    private final float weaponChance;
    private final float offhandChance;
    private final float armorChance;
    private final ArmorSet armorSet;

    private MobGearPlan(Builder builder) {
        this.weapons = List.copyOf(builder.weapons);
        this.offhands = List.copyOf(builder.offhands);
        this.weaponChance = builder.weaponChance;
        this.offhandChance = builder.offhandChance;
        this.armorChance = builder.armorChance;
        this.armorSet = builder.armorSet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasWeapons() {
        return !weapons.isEmpty();
    }

    public float weaponChance() {
        return weaponChance;
    }

    public float offhandChance() {
        return offhandChance;
    }

    public float armorChance() {
        return armorChance;
    }

    public ArmorSet armorSet() {
        return armorSet;
    }

    /** Rolls a random main hand weapon, or an empty stack if the plan has none. */
    public ItemStack rollWeapon(RandomSource random, RegistryAccess access) {
        return roll(weapons, random, access);
    }

    /** Rolls a random off hand item, or an empty stack if the plan has none. */
    public ItemStack rollOffhand(RandomSource random, RegistryAccess access) {
        return roll(offhands, random, access);
    }

    /** Resolves the armor item for a slot from the configured armor set. */
    public Item armorItem(ArmorItem.Type type) {
        return switch (armorSet) {
            case PLATE -> TinkerTools.plateArmor.get(type);
            case TRAVELERS -> TinkerTools.travelersGear.get(type);
            case NONE -> null;
        };
    }

    private static ItemStack roll(List<WeightedItem> entries, RandomSource random, RegistryAccess access) {
        if (entries.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int total = 0;
        for (WeightedItem entry : entries) {
            total += Math.max(1, entry.weight());
        }
        int roll = random.nextInt(total);
        for (WeightedItem entry : entries) {
            roll -= Math.max(1, entry.weight());
            if (roll < 0) {
                return ToolFactory.build(entry.item().get(), random, access);
            }
        }
        return ItemStack.EMPTY;
    }

    public static final class Builder {
        private final List<WeightedItem> weapons = new ArrayList<>();
        private final List<WeightedItem> offhands = new ArrayList<>();
        private float weaponChance = 0.9f;
        private float offhandChance = 0.2f;
        private float armorChance = 0.2f;
        private ArmorSet armorSet = ArmorSet.NONE;

        private Builder() {
        }

        public Builder weapon(Supplier<? extends Item> item, int weight) {
            weapons.add(new WeightedItem(item, weight));
            return this;
        }

        public Builder offhand(Supplier<? extends Item> item, int weight) {
            offhands.add(new WeightedItem(item, weight));
            return this;
        }

        public Builder weaponChance(float chance) {
            this.weaponChance = chance;
            return this;
        }

        public Builder offhandChance(float chance) {
            this.offhandChance = chance;
            return this;
        }

        public Builder armor(float chance, ArmorSet set) {
            this.armorChance = chance;
            this.armorSet = set;
            return this;
        }

        public MobGearPlan build() {
            return new MobGearPlan(this);
        }
    }
}
