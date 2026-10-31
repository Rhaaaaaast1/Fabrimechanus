package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Empty-hand tree punching rules and disabled-item messages. */
public final class PrimitiveEvents {
    private static final int WARN_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> LAST_WARN = new HashMap<>();

    private PrimitiveEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onBreak);
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(PrimitiveEvents::onAttack);
    }

    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        if (player == null || player.isCreative() || !state.is(FabriTags.REQUIRES_AXE)) {
            return;
        }
        if (isAxe(player.getMainHandItem())) {
            return;
        }

        event.setNewSpeed(0.0F);
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }

        long now = player.level().getGameTime();
        long last = LAST_WARN.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now - last < WARN_COOLDOWN_TICKS) {
            return;
        }
        LAST_WARN.put(player.getUUID(), now);

        player.hurt(player.damageSources().generic(), 1.0F);
        var optionalPos = event.getPosition();
        if (optionalPos.isPresent()) {
            var pos = optionalPos.get();
            double dx = player.getX() - (pos.getX() + 0.5D);
            double dz = player.getZ() - (pos.getZ() + 0.5D);
            player.knockback(0.35D, dx, dz);
        }
        player.displayClientMessage(Component.translatable("message.fabrimechanus.no_tree_punching"), true);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.isCreative() || !event.getState().is(FabriTags.REQUIRES_AXE)) {
            return;
        }
        if (!isAxe(player.getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (disable(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (disable(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (disable(event.getEntity(), event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    private static void onAttack(AttackEntityEvent event) {
        if (disable(event.getEntity(), event.getEntity().getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    private static boolean disable(Player player, ItemStack stack) {
        if (player == null || player.isCreative() || stack.isEmpty() || !PrimitiveTargets.contains(stack.getItem())) {
            return false;
        }
        player.displayClientMessage(Component.translatable("tooltip.fabrimechanus.disabled"), true);
        return true;
    }

    private static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.AXES);
    }
}
