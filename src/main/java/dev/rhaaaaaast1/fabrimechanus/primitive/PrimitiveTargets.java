package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/** Vanilla gear that this mod disables and replaces with Tinkers' equipment. */
public final class PrimitiveTargets {
    private static final Set<Item> TARGETS = new HashSet<>();

    static {
        add("wooden_sword", "wooden_pickaxe", "wooden_axe", "wooden_shovel", "wooden_hoe");
        add("stone_sword", "stone_pickaxe", "stone_axe", "stone_shovel", "stone_hoe");
        add("iron_sword", "iron_pickaxe", "iron_axe", "iron_shovel", "iron_hoe");
        add("golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe");
        add("diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe");
        add("netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe");

        add("leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots");
        add("iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots");
        add("golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots");
        add("diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots");
        add("netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots");

        add("flint_and_steel", "shears", "fishing_rod", "bow", "crossbow", "shield");
    }

    private PrimitiveTargets() {
    }

    public static boolean contains(Item item) {
        return TARGETS.contains(item);
    }

    public static Set<Item> all() {
        return Set.copyOf(TARGETS);
    }

    private static void add(String... paths) {
        for (String path : paths) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
            if (item != null && item != Items.AIR) {
                TARGETS.add(item);
            }
        }
    }
}
