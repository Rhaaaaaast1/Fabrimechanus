package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.ItemStackUtil;

/** Creates fully built Tinkers' Construct tools and armor with randomized materials. */
public final class ToolFactory {
    private ToolFactory() {
    }

    /**
     * Builds a single tool or armor piece out of the given item, randomizing every part material.
     *
     * @param item   modifiable item, e.g. {@code TinkerTools.sword.get()}
     * @param random random source, usually the spawning mob's random
     * @param access registry access of the current level, used to keep the stack packet safe
     */
    public static ItemStack build(Item item, RandomSource random, RegistryAccess access) {
        return build(item, random, access, 0);
    }

    /**
     * Builds a tool/armor piece, optionally forcing one material tier for every part.
     *
     * @param materialTier 1-4 for a fixed tier, or 0 to use the global tier weights
     */
    public static ItemStack build(Item item, RandomSource random, RegistryAccess access, int materialTier) {
        if (!(item instanceof IModifiable modifiable)) {
            return new ItemStack(item);
        }
        ToolDefinition definition = modifiable.getToolDefinition();
        ItemStack stack;
        if (definition.hasMaterials()) {
            var materials = materialTier > 0
                    ? RandomTierMaterials.buildMaterials(definition, random, materialTier)
                    : RandomTierMaterials.buildMaterials(definition, random);
            stack = ToolStack.createTool(item, definition, materials).createStack();
        } else {
            stack = new ItemStack(item);
        }
        // a stack that cannot be written into a packet would disconnect every client that sees the mob
        ItemStackUtil.ensurePacketSafe(stack, access);
        return stack;
    }
}
