package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * The vanilla gear that mobs must no longer spawn with.
 *
 * <p>This is deliberately a "strip after vanilla rolled its equipment" filter instead of touching any loot table or
 * spawn code: it removes the listed items from freshly spawned mobs and nothing else, so pick-up behavior, modded gear
 * and everything Tinkers' Construct itself hands out stay untouched.</p>
 */
public final class VanillaGear {
    private static final Set<Item> BLOCKED = new HashSet<>();

    static {
        addToolSet(Items.WOODEN_SWORD, Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE);
        addToolSet(Items.STONE_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE);
        addToolSet(Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE);
        addToolSet(Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);
        addToolSet(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE);
        addToolSet(Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE);

        addArmorSet(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        addArmorSet(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        addArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        addArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        addArmorSet(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        addArmorSet(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        // other vanilla gear that is replaced by Tinkers' Construct equivalents
        BLOCKED.add(Items.SHIELD);
        BLOCKED.add(Items.BOW);
        BLOCKED.add(Items.CROSSBOW);
        BLOCKED.add(Items.SHEARS);
        BLOCKED.add(Items.FLINT_AND_STEEL);
    }

    private VanillaGear() {
    }

    private static void addToolSet(Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        BLOCKED.add(sword);
        BLOCKED.add(pickaxe);
        BLOCKED.add(axe);
        BLOCKED.add(shovel);
        BLOCKED.add(hoe);
    }

    private static void addArmorSet(Item helmet, Item chestplate, Item leggings, Item boots) {
        BLOCKED.add(helmet);
        BLOCKED.add(chestplate);
        BLOCKED.add(leggings);
        BLOCKED.add(boots);
    }

    /** True if the stack holds one of the vanilla items mobs should not spawn with. */
    public static boolean isBlocked(ItemStack stack) {
        return !stack.isEmpty() && BLOCKED.contains(stack.getItem());
    }
}
