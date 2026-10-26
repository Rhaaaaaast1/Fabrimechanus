package dev.rhaaaaaast1.fabrimechanus.kubejs.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaScript-facing wrapper around a block entity's Container / WorldlyContainer
 * inventory, with an IItemHandler fallback for modded machines.
 */
public final class BlockEntitySlots {
    private final BlockEntity blockEntity;
    private final Container container;
    private final IItemHandler itemHandler;

    public BlockEntitySlots(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.container = blockEntity instanceof Container c ? c : null;
        this.itemHandler = this.container == null ? findItemHandler(blockEntity) : null;
    }

    private static IItemHandler findItemHandler(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return null;
        }
        return ((ILevelExtension) level).getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), null);
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Container getContainer() {
        return container;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int size() {
        if (container != null) {
            return container.getContainerSize();
        }
        return itemHandler == null ? 0 : itemHandler.getSlots();
    }

    public ItemStack get(int slot) {
        if (slot < 0 || slot >= size()) {
            return ItemStack.EMPTY;
        }
        if (container != null) {
            return container.getItem(slot).copy();
        }
        return itemHandler.getStackInSlot(slot).copy();
    }

    public List<ItemStack> getAll() {
        List<ItemStack> result = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            result.add(get(i));
        }
        return result;
    }

    public void set(int slot, ItemStack stack) {
        if (slot < 0 || slot >= size()) {
            return;
        }

        ItemStack value = stack == null ? ItemStack.EMPTY : stack.copy();

        if (container != null) {
            container.setItem(slot, value);
            container.setChanged();
        } else if (itemHandler != null) {
            ItemStack existing = itemHandler.getStackInSlot(slot);
            if (!existing.isEmpty()) {
                itemHandler.extractItem(slot, existing.getCount(), false);
            }
            itemHandler.insertItem(slot, value, false);
            blockEntity.setChanged();
        }

        sync();
    }

    public void setAll(List<ItemStack> stacks) {
        int count = Math.min(size(), stacks.size());
        for (int i = 0; i < count; i++) {
            set(i, stacks.get(i));
        }
    }

    public int[] getSlotsForFace(Direction side) {
        if (container instanceof WorldlyContainer worldly) {
            return worldly.getSlotsForFace(side);
        }

        int size = size();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    /**
     * Mark the block entity dirty and ask clients to refresh the block entity.
     * Vanilla containers normally also update an open menu through their own logic.
     */
    public void sync() {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
    }

    public List<ItemStack> snapshot() {
        return getAll();
    }
}
