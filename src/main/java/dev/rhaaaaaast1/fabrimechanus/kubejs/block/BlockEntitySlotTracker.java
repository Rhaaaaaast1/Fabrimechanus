package dev.rhaaaaaast1.fabrimechanus.kubejs.block;

import dev.latvian.mods.kubejs.KubeJS;
import dev.rhaaaaaast1.fabrimechanus.kubejs.KubeJSToolkitEvents;
import dev.rhaaaaaast1.fabrimechanus.kubejs.api.BlockEntitySlots;
import dev.rhaaaaaast1.fabrimechanus.kubejs.event.BlockEntitySlotChangedKubeEvent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cheap slot change detection: compare a snapshot whenever
 * {@link BlockEntity#setChanged()} is called on the server.
 */
public final class BlockEntitySlotTracker {
    private static final Map<BlockEntity, List<ItemStack>> SNAPSHOTS = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> FIRING = ThreadLocal.withInitial(() -> false);

    private BlockEntitySlotTracker() {
    }

    public static void onChanged(BlockEntity blockEntity) {
        if (!ModList.get().isLoaded("kubejs") || !KubeJSToolkitEvents.hasSlotListeners()) {
            return;
        }
        if (FIRING.get() || !(blockEntity instanceof Container)) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        List<ItemStack> current = new BlockEntitySlots(blockEntity).getAll();
        List<ItemStack> previous = SNAPSHOTS.put(blockEntity, current);
        if (previous == null) {
            return;
        }

        FIRING.set(true);
        try {
            int size = Math.min(previous.size(), current.size());
            for (int slot = 0; slot < size; slot++) {
                ItemStack oldStack = previous.get(slot);
                ItemStack newStack = current.get(slot);
                if (same(oldStack, newStack)) {
                    continue;
                }

                BlockEntitySlotChangedKubeEvent event = new BlockEntitySlotChangedKubeEvent(
                        level, blockEntity, slot, oldStack, newStack
                );
                KubeJSToolkitEvents.BLOCK_ENTITY_SLOT_CHANGED.post(event);

                KubeJS.LOGGER.info("[kubejs-toolkit] blockEntitySlotChanged {} slot {}: {} -> {}",
                        blockEntity.getBlockPos(), slot, oldStack.getItem(), newStack.getItem());
            }
        } catch (Throwable throwable) {
            KubeJS.LOGGER.error("[kubejs-toolkit] blockEntitySlotChanged failed for {}", blockEntity, throwable);
        } finally {
            FIRING.set(false);
        }
    }

    public static List<ItemStack> snapshot(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return new ArrayList<>();
        }
        return new BlockEntitySlots(blockEntity).getAll();
    }

    public static void clear() {
        SNAPSHOTS.clear();
    }

    private static boolean same(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount()
                && ItemStack.isSameItemSameComponents(first, second);
    }
}
