package dev.rhaaaaaast1.fabrimechanus.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Hopper-facing output container for a full composter. */
public final class ComposterOutputContainer implements WorldlyContainer {
    private final BlockState state;
    private final LevelAccessor level;
    private final BlockPos pos;
    private boolean taken;

    public ComposterOutputContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        this.state = state;
        this.level = level;
        this.pos = pos;
    }

    private boolean ready() {
        return !taken && state.getValue(ComposterBlock.LEVEL) == 8;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return !ready();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 && ready() ? new ItemStack(ComposterOutput.get()) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot != 0 || count <= 0 || !ready()) {
            return ItemStack.EMPTY;
        }
        taken = true;
        ItemStack output = new ItemStack(ComposterOutput.get(), count);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setBlock(pos, state.setValue(ComposterBlock.LEVEL, 0), 3);
            serverLevel.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return output;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 1);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? new int[]{0} : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 0 && direction == Direction.DOWN;
    }

    @Override
    public void clearContent() {
    }
}
