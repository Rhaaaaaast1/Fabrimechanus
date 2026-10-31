package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        if (player == null || player.isCreative() || !requiresAxe(state)) {
            return;
        }
        if (isAxe(player.getMainHandItem())) {
            return;
        }

        event.setNewSpeed(0.0F);
        if (player.getMainHandItem().isEmpty()) {
            event.getPosition().ifPresent(pos -> punishEmptyHand(player, pos));
        }
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.isCreative() || !requiresAxe(event.getState())) {
            return;
        }
        if (!isAxe(player.getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty()) {
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            if (level.getBlockState(pos).is(FabriTags.PLANT_FIBER_SOURCES)) {
                if (!level.isClientSide()) {
                    level.destroyBlock(pos, false);
                    int count = 1 + level.random.nextInt(2);
                    Block.popResource(level, pos, new ItemStack(PrimitiveRegistry.PLANT_FIBER.get(), count));
                    level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
                    player.swing(InteractionHand.MAIN_HAND, true);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
        if (disable(player, event.getItemStack())) {
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
        Player player = event.getEntity();
        if (!player.isCreative() && player.getMainHandItem().isEmpty()
                && requiresAxe(event.getLevel().getBlockState(event.getPos()))) {
            if (!player.level().isClientSide()) {
                punishEmptyHand(player, event.getPos());
            }
            event.setCanceled(true);
            return;
        }
        if (disable(player, event.getItemStack())) {
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

    private static void punishEmptyHand(Player player, BlockPos pos) {
        long now = player.level().getGameTime();
        long last = LAST_WARN.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now - last < WARN_COOLDOWN_TICKS) {
            return;
        }
        LAST_WARN.put(player.getUUID(), now);

        player.hurt(player.damageSources().generic(), 1.0F);
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        player.knockback(0.35D, dx, dz);
        player.displayClientMessage(Component.translatable("message.fabrimechanus.no_tree_punching"), true);
    }

    private static boolean requiresAxe(BlockState state) {
        return state.is(FabriTags.REQUIRES_AXE)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.STANDING_SIGNS)
                || state.is(BlockTags.WALL_SIGNS)
                || state.is(BlockTags.ALL_HANGING_SIGNS)
                || state.is(BlockTags.BUTTONS)
                || state.is(BlockTags.PRESSURE_PLATES);
    }

    private static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.AXES);
    }
}
