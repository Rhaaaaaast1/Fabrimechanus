package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Creates fully built Tinkers' Construct tools and armor with randomized materials. */
public final class ToolFactory {
    private ToolFactory() {
    }

    /**
     * Builds a single tool or armor piece out of the given item, randomizing every part material.
     *
     * @param item   modifiable item, e.g. {@code TinkerTools.sword.get()}
     * @param random random source, usually the spawning mob's random
     */
    public static ItemStack build(Item item, RandomSource random) {
        if (!(item instanceof IModifiable modifiable)) {
            return new ItemStack(item);
        }
        ToolDefinition definition = modifiable.getToolDefinition();
        ItemStack stack;
        if (definition.hasMaterials()) {
            stack = ToolStack.createTool(item, definition, RandomTierMaterials.buildMaterials(definition, random)).createStack();
        } else {
            stack = new ItemStack(item);
        }
        return stack;
    }
}
