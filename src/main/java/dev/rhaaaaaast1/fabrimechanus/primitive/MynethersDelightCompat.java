package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Optional My Nether's Delight sawing support. */
public final class MynethersDelightCompat {
    private static final ResourceLocation POWDERY_BLOCK =
            ResourceLocation.fromNamespaceAndPath("mynethersdelight", "powdery_block");
    private static final ResourceLocation STRIPPED_POWDERY_BLOCK =
            ResourceLocation.fromNamespaceAndPath("mynethersdelight", "stripped_powdery_block");
    private static final ResourceLocation POWDERY_PLANKS =
            ResourceLocation.fromNamespaceAndPath("mynethersdelight", "powdery_planks");

    private MynethersDelightCompat() {
    }

    public static boolean isPowderyBlock(BlockState state) {
        return state.is(block(POWDERY_BLOCK));
    }

    public static boolean isStrippedPowderyBlock(BlockState state) {
        return state.is(block(STRIPPED_POWDERY_BLOCK));
    }

    public static Block planksFor(BlockState state) {
        if (!isStrippedPowderyBlock(state)) {
            return null;
        }
        Block planks = block(POWDERY_PLANKS);
        return planks == Blocks.AIR ? null : planks;
    }

    private static Block block(ResourceLocation id) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(id);
    }
}
