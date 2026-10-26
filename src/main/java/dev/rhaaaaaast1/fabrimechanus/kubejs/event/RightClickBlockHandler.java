package dev.rhaaaaaast1.fabrimechanus.kubejs.event;

import dev.latvian.mods.kubejs.KubeJS;
import dev.rhaaaaaast1.fabrimechanus.kubejs.KubeJSToolkitEvents;
import dev.rhaaaaaast1.fabrimechanus.kubejs.block.BlockEntitySlotTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class RightClickBlockHandler {
    private RightClickBlockHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !KubeJSToolkitEvents.hasRightClickListeners()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Block block = level.getBlockState(pos).getBlock();
        List<ItemStack> slots = BlockEntitySlotTracker.snapshot(blockEntity);

        RightClickBlockPreKubeEvent kubeEvent = new RightClickBlockPreKubeEvent(
                player,
                event.getHand(),
                level,
                pos,
                block,
                blockEntity,
                event.getItemStack(),
                event.getHitVec(),
                slots
        );

        KubeJSToolkitEvents.RIGHT_CLICK_BLOCK_PRE.post(kubeEvent);

        if (kubeEvent.canceled) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            KubeJS.LOGGER.info("[kubejs-toolkit] rightClickBlockPre canceled {} at {} by {}",
                    block, pos, player.getName().getString());
        }
    }
}
