package dev.rhaaaaaast1.fabrimechanus.kubejs.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Static KubeJS binding for block entity inventories.
 *
 * <pre>{@code
 * let slots = BlockEntityAPI.wrap(level, pos)
 * slots.set(0, Item.of('minecraft:gold_ingot', { custom_data: { foo: 1 } }))
 * slots.sync()
 * }</pre>
 */
public final class BlockEntityAPI {
    private BlockEntityAPI() {
    }

    public static BlockEntitySlots wrap(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? null : new BlockEntitySlots(blockEntity);
    }

    public static BlockEntitySlots wrap(BlockEntity blockEntity) {
        return blockEntity == null ? null : new BlockEntitySlots(blockEntity);
    }

    public static List<ItemStack> snapshot(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return List.of();
        }
        return new BlockEntitySlots(blockEntity).getAll();
    }

    public static List<ItemStack> snapshot(Level level, BlockPos pos) {
        BlockEntity blockEntity = level == null || pos == null ? null : level.getBlockEntity(pos);
        return snapshot(blockEntity);
    }

    public static List<ItemStack> copyList(List<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            result.add(stack.copy());
        }
        return result;
    }
}
