package dev.rhaaaaaast1.fabrimechanus.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.rhaaaaaast1.fabrimechanus.kubejs.api.BlockEntityAPI;
import dev.rhaaaaaast1.fabrimechanus.kubejs.api.BlockEntitySlots;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Fired before a player right-clicks a block. Set {@link #canceled} or call
 * {@link #cancel()} to deny both block and item use.
 */
public class RightClickBlockPreKubeEvent implements KubeEvent {
    public final Player player;
    public final InteractionHand hand;
    public final Level level;
    public final BlockPos pos;
    public final Block block;
    public final BlockEntity blockEntity;
    public final ItemStack item;
    public final List<ItemStack> slots;
    public final BlockHitResult hit;

    public boolean canceled;

    public RightClickBlockPreKubeEvent(Player player, InteractionHand hand, Level level, BlockPos pos,
                                       Block block, BlockEntity blockEntity, ItemStack item,
                                       BlockHitResult hit, List<ItemStack> slots) {
        this.player = player;
        this.hand = hand;
        this.level = level;
        this.pos = pos;
        this.block = block;
        this.blockEntity = blockEntity;
        this.item = item == null ? ItemStack.EMPTY : item.copy();
        this.hit = hit;
        this.slots = BlockEntityAPI.copyList(slots == null ? List.of() : slots);
    }

    public void cancel() {
        this.canceled = true;
    }

    public BlockEntitySlots container() {
        return BlockEntityAPI.wrap(blockEntity);
    }
}
