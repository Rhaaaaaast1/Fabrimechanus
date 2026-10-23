package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.world.item.ItemStack;

/**
 * The vanilla gear that mobs must no longer spawn with.
 *
 * <p>The list itself lives in the {@code fabrimechanus:stripped_monster_gear} item tag
 * ({@code data/fabrimechanus/tags/item/stripped_monster_gear.json}), so a pack can drop one entry or add modded
 * weapons without patching code. Removing gear is still done after vanilla rolled the equipment: nothing here touches
 * loot tables or spawn code, so pick-up behavior and Tinkers' Construct's own mob loot stay untouched.</p>
 */
public final class VanillaGear {
    private VanillaGear() {
    }

    /** True if the stack holds one of the items the {@code stripped_monster_gear} tag lists. */
    public static boolean isBlocked(ItemStack stack) {
        return !stack.isEmpty() && stack.is(FabriTags.STRIPPED_MONSTER_GEAR);
    }
}
