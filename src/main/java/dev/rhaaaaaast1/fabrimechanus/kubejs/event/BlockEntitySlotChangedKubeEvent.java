package dev.rhaaaaaast1.fabrimechanus.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.rhaaaaaast1.fabrimechanus.kubejs.api.BlockEntitySlots;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Fired after a container slot changed on the server. Scripts may replace the
 * new stack with {@link #setStack(ItemStack)}.
 */
public class BlockEntitySlotChangedKubeEvent implements KubeEvent {
    public final Level level;
    public final BlockPos pos;
    public final Block block;
    public final BlockEntity blockEntity;
    public final int slot;
    public final ItemStack oldStack;
    public final ItemStack newStack;

    public BlockEntitySlotChangedKubeEvent(Level level, BlockEntity blockEntity, int slot, ItemStack oldStack, ItemStack newStack) {
        this.level = level;
        this.pos = blockEntity.getBlockPos();
        this.block = level.getBlockState(pos).getBlock();
        this.blockEntity = blockEntity;
        this.slot = slot;
        this.oldStack = oldStack == null ? ItemStack.EMPTY : oldStack.copy();
        this.newStack = newStack == null ? ItemStack.EMPTY : newStack.copy();
    }

    public void setStack(ItemStack stack) {
        BlockEntitySlots slots = new BlockEntitySlots(blockEntity);
        slots.set(slot, stack);
    }

    public ItemStack getStack() {
        return new BlockEntitySlots(blockEntity).get(slot);
    }
}
